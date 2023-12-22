package com.github.fishlikewater.server.handle.socks;

import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.BootStrapFactroy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author fishlikewater@126.com
 * @since 2022年08月20日 22:51
 **/

@Slf4j
@RequiredArgsConstructor
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private final ProxyConfig proxyConfig;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {

                }
            });
            if (proxyConfig.isUseLocalPorts()) {
                final int oneLocalPort = proxyConfig.getOneLocalPort();
                log.debug("本次使用本地端口:{}", oneLocalPort);
                bootstrap.localAddress(oneLocalPort);
            }
            ChannelFuture future1 = bootstrap.connect(msg.dstAddr(), msg.dstPort());
            future1.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("成功连接目标服务器");
                    if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                        ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                    }
                    ctx.pipeline().addLast(new Client2DestHandler(future));
                    future.channel().pipeline().addLast(new Dest2ClientHandler(ctx));
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    ctx.writeAndFlush(commandResponse);
                } else {
                    log.debug("连接目标服务器失败");
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    ctx.writeAndFlush(commandResponse);
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
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
        public void channelRead(ChannelHandlerContext ctx2, Object destMsg) {
            log.trace("将目标服务器信息转发给客户端");
            clientChannelContext.writeAndFlush(destMsg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx2) {
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
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.trace("将客户端的消息转发给目标服务器端");
            destChannelFuture.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
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
