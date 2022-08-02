package com.github.fishlikewater.proxy.handler.socks_client;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.BootStrapFactroy;
import com.github.fishlikewater.proxy.handler.socks_proxy.Socks5ProxyCommandRequestHandler;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @Description:
 * @date: 2022年07月29日 14:18
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
public class Client2DestHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private final ProxyConfig proxyConfig;

    public Client2DestHandler(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        final MessageProbuf.MessageType type = msg.getType();
        switch (type) {
            case REQUEST:
                final String requestId = msg.getRequestId();
                final MessageProbuf.SocksMsg socksMsg = msg.getSocksMsg();
                log.info("准备连接目标服务器");
                Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
                log.info("连接目标服务器");
                ChannelFuture future = bootstrap.connect(socksMsg.getDstAddr(), socksMsg.getDstPort());
                future.addListener(new ChannelFutureListener() {
                    public void operationComplete(final ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.info("成功连接目标服务器");
                            //future.channel().writeAndFlush()
                            /*if (ctx.pipeline().get(Client2DestHandler.class) != null) {
                                ctx.pipeline().remove(Client2DestHandler.class);
                            }*/
                            //ctx.pipeline().addLast(new Client2DestHandler.Client2DestHandler2(future));
                            future.channel().pipeline().addLast(new Client2DestHandler.Dest2ClientHandler(requestId, ctx));
                        } else {
                            log.info("连接目标服务器失败");
                        }
                    }
                });
                break;
            case RESPONSE:
                log.info(msg.getExtend());
                break;
            case VALID:
                String extend = msg.getExtend();
                if (!"SUCCESS".equals(extend)) {
                    log.warn(extend);
                } else {
                    log.info("验证成功");
                }
                break;
            case HEALTH:
                log.info("get receipt health packet from server");
                break;
            case CLOSE:
                ctx.channel().close();
        }
    }

    /**
     * 将目标服务器信息转发给客户端
     *
     * @author huchengyi
     */
    private static class Dest2ClientHandler extends SimpleChannelInboundHandler<Object> {

        private final String requestId;

        private final ChannelHandlerContext clientChannelContext;

        public Dest2ClientHandler(String requestId, ChannelHandlerContext clientChannelContext) {
            this.clientChannelContext = clientChannelContext;
            this.requestId = requestId;
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
        public void channelRead0(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
            log.info("将目标服务器信息转发给服务端");
            MessageProbuf.Response.Builder builder = MessageProbuf.Response.newBuilder();
            builder.setBody(ByteString.copyFrom(ObjectUtil.serialize(destMsg)));
            clientChannelContext.writeAndFlush(MessageProbuf.Message.newBuilder()
                    .setType(MessageProbuf.MessageType.RESPONSE)
                    .setResponse(builder.build())
                    .setRequestId(requestId));
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
    private static class Client2DestHandler2 extends ChannelInboundHandlerAdapter {

        private final ChannelFuture destChannelFuture;

        public Client2DestHandler2(ChannelFuture destChannelFuture) {
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
