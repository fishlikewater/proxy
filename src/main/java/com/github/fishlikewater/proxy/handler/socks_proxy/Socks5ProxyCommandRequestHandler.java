package com.github.fishlikewater.proxy.handler.socks_proxy;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxy.handler.BootStrapFactroy;
import com.github.fishlikewater.proxy.handler.proxy_server.CacheUtil;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.IdUtil;
import com.github.fishlikewater.proxy.kit.IpCacheKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class Socks5ProxyCommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private Bootstrap bootstrap;

    private ChannelFuture future;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final boolean b = ChannelGroupKit.removeChannel(ctx.channel());
        if (!b) {
            IpCacheKit.remove(ctx.channel());
        }
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        //log.info("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        final Channel clientChannel = IpCacheKit.getIpsMap().get(msg.dstAddr());
        log.info("类型: {}", msg.type());
        if (Objects.isNull(clientChannel)) {
            //log.info("没有目标地址");
            if (msg.type().equals(Socks5CommandType.CONNECT)) {
                log.trace("准备连接目标服务器");
                bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
                log.trace("连接目标服务器");
                future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
                future.addListener(new ChannelFutureListener() {
                    public void operationComplete(final ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.trace("成功连接目标服务器");
                            if (ctx.pipeline().get(Socks5ProxyCommandRequestHandler.class) != null) {
                                ctx.pipeline().remove(Socks5ProxyCommandRequestHandler.class);
                            }
                            ctx.pipeline().addLast(new Socks5ProxyCommandRequestHandler.Client2DestHandler(future));
                            future.channel().pipeline().addLast(new Socks5ProxyCommandRequestHandler.Dest2ClientHandler(ctx));
                            Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                            ctx.writeAndFlush(commandResponse);
                        } else {
                            log.debug("连接目标服务器失败");
                            Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                            ctx.writeAndFlush(commandResponse);
                        }
                    }

                });
            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            if (msg.type().equals(Socks5CommandType.CONNECT)) {
                MessageProbuf.SocksMsg.Builder builder = MessageProbuf.SocksMsg.newBuilder();
                builder.setType(1).setDstAddr("127.0.0.1").setDstPort(msg.dstPort());
                String requestId = IdUtil.next();
                clientChannel.writeAndFlush(MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.REQUEST)
                        .setSocksMsg(builder.build())
                        .setRequestId(requestId)).addListener((f) -> {
                    if (f.isSuccess()) {
                        CacheUtil.put(requestId, ctx.channel(), 10);
                        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                        ctx.writeAndFlush(commandResponse);
                    } else {
                        log.info("转送失败");
                    }
                });
            } else {
                log.debug("连接目标服务器失败");
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                ctx.writeAndFlush(commandResponse);
            }

        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * 将目标服务器信息转发给客户端
     *
     * @author huchengyi
     */
    private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {

        private final ChannelHandlerContext clientChannelContext;

        public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
            this.clientChannelContext = clientChannelContext;
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            boolean canWrite = ctx.channel().isWritable();
            log.trace(ctx.channel() + " 可写性：" + canWrite);
            //流量控制，不允许继续读
            clientChannelContext.channel().config().setAutoRead(canWrite);
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
            log.trace("将目标服务器信息转发给客户端");
            clientChannelContext.writeAndFlush(destMsg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
            log.trace("目标服务器断开连接");
            clientChannelContext.channel().close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof IOException) {
                // 远程主机强迫关闭了一个现有的连接的异常
                ctx.close();
            } else {
                super.exceptionCaught(ctx, cause);
            }
        }
    }

    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
    private static class Client2DestHandler extends ChannelInboundHandlerAdapter {

        private final ChannelFuture destChannelFuture;

        public Client2DestHandler(ChannelFuture destChannelFuture) {
            this.destChannelFuture = destChannelFuture;
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            boolean canWrite = ctx.channel().isWritable();
            log.trace(ctx.channel() + " 可写性：" + canWrite);
            //流量控制，不允许继续读
            destChannelFuture.channel().config().setAutoRead(canWrite);
            super.channelWritabilityChanged(ctx);
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.trace("将客户端的消息转发给目标服务器端");
            destChannelFuture.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.trace("客户端断开连接");
            destChannelFuture.channel().close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof IOException) {
                // 远程主机强迫关闭了一个现有的连接的异常
                ctx.close();
            } else {
                super.exceptionCaught(ctx, cause);
            }
        }
    }

}
