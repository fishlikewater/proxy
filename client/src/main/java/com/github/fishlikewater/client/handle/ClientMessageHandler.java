/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月19日 13:03
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ProxyClient client;

    public ClientMessageHandler(ProxyClient client) {
        this.client = client;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws InterruptedException {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        switch (cmd) {
            case AUTH:
                HandleKit.toRegister(msg, ctx, client.getProxyConfig());
                break;
            case REGISTER:
                if (msg.getState() == 1) {
                    log.info("本机分配的虚拟ip为: " + new String(msg.getBytes(), StandardCharsets.UTF_8));
                    HandleKit.afterRegister(ctx, client.getProxyConfig());
                } else {
                    final EventLoop loop = ctx.channel().eventLoop();
                    msg.setState((byte) 1);
                    loop.schedule(() -> HandleKit.toRegister(msg, ctx, client.getProxyConfig()), 30, TimeUnit.SECONDS);
                }
                break;
            case DATA_CHANNEL:
                HandleKit.createDataChannel(ctx, client.getProxyConfig(), new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case DATA_CHANNEL_ACK:
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case HEALTH:
                log.debug("get health info");
                break;
            case REQUEST:
                HandleKit.handlerRequest(msg, ctx, client.getProxyConfig());
                break;
            case RESPONSE:
                final Channel socksChannel = ctx.channel().attr(Socks5Kit.CHANNELS_SOCKS).get().get(msg.getId());
                if (Objects.nonNull(socksChannel) && socksChannel.isActive()) {
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(msg.getBytes().length);
                    buf.writeBytes(msg.getBytes());
                    socksChannel.writeAndFlush(buf);
                }
                break;
            case CONNECTION:
                HandleKit.handlerConnection2(msg, ctx, client.getProxyConfig());
                break;
            case ACK:
                HandleKit.handlerAck(ctx, msg);
                break;
            default:
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            log.error("happen error: ", cause);
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
