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
package com.github.fishlikewater.socks5.handle;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.socks5.boot.BootStrapFactory;
import com.github.fishlikewater.socks5.config.Socks5Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author fishl
 */
@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private final Socks5Config socks5Config;

    private final Map<String, String> ipMapping;

    public Socks5CommandRequestHandler(Socks5Config socks5Config) {
        this.socks5Config = socks5Config;
        ipMapping = new HashMap<>();
        final Socks5Config.Mapping[] mapping = socks5Config.getMapping();
        if (Objects.nonNull(mapping)) {
            for (Socks5Config.Mapping mapping1 : mapping) {
                ipMapping.put(mapping1.getRequestIp(), mapping1.getMappingIp());
            }
        }
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            final Long requestId = IdUtil.id();
            ctx.channel().attr(Socks5Kit.LOCAL_INFO).set(requestId);
            final MessageProtocol.Dst dst = new MessageProtocol.Dst();
            String dstAddr = msg.dstAddr();
            final String ip = ipMapping.get(dstAddr);
            if (Objects.nonNull(ip)) {
                dstAddr = ip;
            }
            final String filterIp = socks5Config.getFilterIp();
            if (CharSequenceUtil.isNotBlank(filterIp)) {
                if (!CharSequenceUtil.startWith(dstAddr, filterIp)) {
                    handlerLocal(ctx, msg);
                } else {
                    handlerProxy(ctx, msg, dst, dstAddr, requestId);
                }
            } else {
                handlerProxy(ctx, msg, dst, dstAddr, requestId);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlerProxy(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg, MessageProtocol.Dst dst, String dstAddr, Long requestId) {
        dst.setDstAddress(dstAddr);
        dst.setDstPort(msg.dstPort());
        Socks5Kit.getChannel().attr(Socks5Kit.CHANNELS_SOCKS).get().put(requestId, ctx.channel());
        final ChannelPipeline pipeline = ctx.channel().pipeline();
        if (socks5Config.isCheckConnect()) {
            this.mustCheckConnection(ctx, dst, requestId);
        } else {
            this.notCheckConnection(ctx, dst, requestId, pipeline);
        }
    }

    private void mustCheckConnection(ChannelHandlerContext ctx, MessageProtocol.Dst dst, Long requestId) {
        MessageProtocol message = new MessageProtocol();
        message
                .setCmd(MessageProtocol.CmdEnum.CONNECTION)
                .setId(requestId)
                .setDst(dst)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
        Socks5Kit.getChannel().writeAndFlush(message).addListener(future -> {
            if (future.isSuccess()) {
                if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                    ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                }
                ctx.pipeline().remove(Socks5InitialAuthHandler.class);
                ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
                ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
                ctx.pipeline().addLast(new Client2DestHandler(requestId, dst));
            } else {
                log.info("无法连接目标");
            }
        });
    }

    private void notCheckConnection(ChannelHandlerContext ctx, MessageProtocol.Dst dst, Long requestId, ChannelPipeline pipeline) {
        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
        pipeline.remove(Socks5InitialAuthHandler.class);
        pipeline.remove(Socks5InitialRequestDecoder.class);
        pipeline.remove(Socks5CommandRequestDecoder.class);
        pipeline.addLast(new Client2DestHandler(requestId, dst));
        if (pipeline.get(Socks5CommandRequestHandler.class) != null) {
            pipeline.remove(Socks5CommandRequestHandler.class);
        }
        if (ctx.pipeline().get(Socks5PasswordAuthRequestHandler.class) != null) {
            ctx.pipeline().remove(Socks5PasswordAuthRequestHandler.class);
        }
        if (ctx.pipeline().get(Socks5PasswordAuthRequestDecoder.class) != null) {
            ctx.pipeline().remove(Socks5PasswordAuthRequestDecoder.class);
        }
        ctx.channel().writeAndFlush(commandResponse);
    }

    private static void handlerLocal(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        Bootstrap bootstrap = BootStrapFactory.getBootstrap(ctx);
        bootstrap.remoteAddress(msg.dstAddr(), msg.dstPort());
        bootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("成功连接目标服务器");
                if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                    ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                }
                ctx.pipeline().remove(Socks5InitialAuthHandler.class);
                ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
                ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
                ctx.pipeline().addLast(new Client2LocalHandler(future.channel()));
                future.channel().pipeline().addLast(new Local2ClientHandler(ctx.channel()));
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                ctx.writeAndFlush(commandResponse);
            } else {
                log.debug("连接目标服务器失败");
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                ctx.writeAndFlush(commandResponse);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
    private static class Client2DestHandler extends SimpleChannelInboundHandler<Object> {

        private final Long requestId;

        private final MessageProtocol.Dst dst;

        public Client2DestHandler(Long requestId, MessageProtocol.Dst dst) {
            this.requestId = requestId;
            this.dst = dst;
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            boolean canWrite = ctx.channel().isWritable();
            log.trace(ctx.channel() + " 可写性：" + canWrite);
            //流量控制，不允许继续读
            Socks5Kit.getChannel().config().setAutoRead(canWrite);
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) {
            log.debug("将客户端的消息转发给目标服务器端");
            final ByteBuf buf = (ByteBuf) msg;
            final MessageProtocol message = new MessageProtocol();
            message
                    .setId(requestId)
                    .setDst(dst)
                    .setCmd(MessageProtocol.CmdEnum.REQUEST)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setBytes(ByteBufUtil.getBytes(buf));
            Socks5Kit.getChannel().writeAndFlush(message);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            final Long closeId = ctx.channel().attr(Socks5Kit.LOCAL_INFO).get();
            if (ObjectUtil.isNotNull(requestId)) {
                Socks5Kit.getChannel().attr(Socks5Kit.CHANNELS_SOCKS).get().remove(closeId);
            }
            log.debug("客户端断开连接");
            final MessageProtocol message = new MessageProtocol();
            message
                    .setId(closeId)
                    .setCmd(MessageProtocol.CmdEnum.CLOSE)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
            Socks5Kit.getChannel().writeAndFlush(message);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof IOException) {
                ctx.close();
            } else {
                super.exceptionCaught(ctx, cause);
            }
        }
    }
}
