package com.github.fishlikewater.proxy.handler.socks_client;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.proxy.boot.NettyProxyClient;
import com.github.fishlikewater.proxy.gui.ConnectionUtils;
import com.github.fishlikewater.proxy.handler.NoneClientInitializer;
import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @date: 2022年07月29日 14:18
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
public class Client2DestHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private final NettyProxyClient client;

    private Bootstrap clientstrap;

    //保留全局ctx
    private ChannelHandlerContext ctx;

    public Client2DestHandler(NettyProxyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (ConnectionUtils.isRetry()) {
            log.warn("this service has dropped, will retry");
            final EventLoop loop = ctx.channel().eventLoop();
            loop.schedule(client::start, 30, TimeUnit.SECONDS);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        final MessageProbuf.MessageType type = msg.getType();
        switch (type) {
            case REQUEST:
                final String requestId = msg.getRequestId();
                final MessageProbuf.SocksMsg socksMsg = msg.getSocksMsg();
                /*final Socks5CommandRequest commandRequest =
                        new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.IPv4, socksMsg.getDstAddr(), socksMsg.getDstPort());*/
                log.info("准备连接目标服务器");
                Promise<Channel> promise = createPromise(socksMsg.getDstAddr(), socksMsg.getDstPort());
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ToServerHandler(requestId));
                        //channelFuture.get().writeAndFlush(commandRequest);
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

    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        Bootstrap bootstrap = bootstrapConfig();
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        promise.setSuccess(channelFuture.channel());
                        log.info("connection success address {}, port {}", host, port);
                    } else {
                        log.warn("connection fail address {}, port {}", host, port);
                        ConnectionUtils.setStateText(StrUtil.format("connection fail address {}, port {}", host, port));
                        channelFuture.cancel(true);
                    }
                });
        return promise;
    }

    private Bootstrap bootstrapConfig() {
        if (clientstrap == null) clientstrap = new Bootstrap();
        else return this.clientstrap;
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            clientstrap.channel(EpollSocketChannel.class);
        } else {
            clientstrap.channel(NioSocketChannel.class);
        }
        clientstrap.group(ctx.channel().eventLoop());
        clientstrap.handler(new NoneClientInitializer());
        return clientstrap;
    }

    private static class ToServerHandler extends SimpleChannelInboundHandler<Object> {

        private final String requestId;

        public ToServerHandler(String requestId) {
            this.requestId = requestId;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            final MessageProbuf.Response.Builder resp = MessageProbuf.Response.newBuilder();
            ChannelKit.sendMessage(MessageProbuf.Message.newBuilder()
                    .setRequestId(requestId)
                    .setResponse(resp.build())
                    .setType(MessageProbuf.MessageType.RESPONSE).build(), t->{

            });
        }
    }

}

