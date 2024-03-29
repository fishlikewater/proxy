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

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月22日 18:59
 **/
@Slf4j
public class ClientDataHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        Channel channel;
        final long requestId = msg.getId();
        switch (msg.getCmd()) {
            case HEALTH:
                log.debug("get health info");
                break;
            case DATA_CHANNEL_ACK:
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>(16));
                break;
            case CLOSE:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)) {
                    channel.close();
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
                }
                break;
            case REQUEST:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)) {
                    channel.writeAndFlush(msg.getBytes());
                }
                break;
            case RESPONSE:
                final Channel socksChannel = ctx.channel().attr(Socks5Kit.CHANNELS_SOCKS).get().get(msg.getId());
                if (socksChannel != null && socksChannel.isActive()) {
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(msg.getBytes().length);
                    buf.writeBytes(msg.getBytes());
                    socksChannel.writeAndFlush(buf);
                }
                break;
            case CONNECTION:
                HandleKit.handlerConnection(msg, ctx);
                break;
            case ACK:
                HandleKit.handlerAck(ctx, msg);
                break;
            default:
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("数据传输通道断开");
        final Map<Long, Channel> longChannelMap = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get();
        longChannelMap.forEach((k, v) -> v.close());
        longChannelMap.clear();
        super.channelInactive(ctx);
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
