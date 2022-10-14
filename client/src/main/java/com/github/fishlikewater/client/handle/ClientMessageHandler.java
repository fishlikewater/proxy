package com.github.fishlikewater.client.handle;


import com.github.fishlikewater.client.boot.ClientHandlerInitializer;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author zhangx
 * @version V1.0
 * @date 2018年12月26日 10:52
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    //保留全局ctx
    private final ProxyConfig proxyConfig;
    private Bootstrap clientstrap;
    private ChannelHandlerContext ctx;

    public ClientMessageHandler(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
        //log.info("连接活动");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
        //super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws InterruptedException {
        MessageProbuf.MessageType type = msg.getType();
        final MessageProbuf.Protocol protocol = msg.getProtocol();
        if (protocol == MessageProbuf.Protocol.HTTP) {
            handleHttp(ctx, msg, type);
        }
        if (protocol == MessageProbuf.Protocol.TCP) {
            final Map<String, String> headerMap = msg.getRequest().getHeaderMap();
            final String flag = headerMap.get("address") + ":" +  headerMap.get("port");
            if (type == MessageProbuf.MessageType.CLOSE) {
                final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(flag);
                if (channel != null && channel.isActive()) {
                    channel.close();
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(flag);
                }
            } else {
                final byte[] bytes = msg.getRequest().getBody().toByteArray();
                if (type == MessageProbuf.MessageType.REQUEST) {
                    //先判断是否建立连接
                    final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(flag);
                    if (channel != null && channel.isActive()) {
                        channel.writeAndFlush(bytes);
                        log.info("发送tcp数据到目标地址");
                    } else {
                        Bootstrap bootstrap = bootstrapConfig();
                        bootstrap.handler(new ClientHandlerInitializer(proxyConfig, ProxyType.tcp_client));
                        bootstrap.remoteAddress(headerMap.get("address"), Integer.parseInt(headerMap.get("port")));
                        bootstrap.connect().addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(flag, future.channel());
                                future.channel().attr(ChannelKit.LOCAL_INFO).set(flag);
                                future.channel().writeAndFlush(bytes);
                                log.info("连接成功");
                            } else {
                                log.warn("连接失败");
                            }
                        });

                    }
                }
            }
        }
    }

    private void handleHttp(ChannelHandlerContext ctx, MessageProbuf.Message msg, MessageProbuf.MessageType type) {
        switch (type) {
            case REQUEST:
                String requestid = msg.getRequestId();
                final Map<String, String> headerMap = msg.getRequest().getHeaderMap();
                MessageProbuf.Request request = msg.getRequest();
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(request.getHttpVersion()), HttpMethod.valueOf(request.getMethod()), request.getUrl());
                request.getHeaderMap().forEach((key, value) -> req.headers().set(key, value));
                req.headers().set("Host", headerMap.get("address") + ":" + headerMap.get("port"));
                req.content().writeBytes(request.getBody().toByteArray());
                Promise<Channel> promise = createPromise(headerMap.get("address"), Integer.parseInt(headerMap.get("port")));
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ToServerHandler(requestid));
                        channelFuture.get().writeAndFlush(req);
                    }
                });
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


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        Bootstrap bootstrap = bootstrapConfig();
        bootstrap.handler(new TempClientServiceInitializer());
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        promise.setSuccess(channelFuture.channel());
                    } else {
                        log.warn("connection fail address {}, port {}", host, port);
                        channelFuture.cancel(true);
                    }
                });
        return promise;
    }

    private Bootstrap bootstrapConfig() {
        if (clientstrap == null) clientstrap = new Bootstrap();
        else return this.clientstrap;
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.SO_KEEPALIVE, true);
        clientstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 60 * 1000);
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            clientstrap.channel(EpollSocketChannel.class);
        } else {
            clientstrap.channel(NioSocketChannel.class);
        }
        clientstrap.group(ctx.channel().eventLoop());
        return clientstrap;
    }
}
