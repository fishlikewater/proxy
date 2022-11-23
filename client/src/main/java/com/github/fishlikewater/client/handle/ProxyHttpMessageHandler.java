package com.github.fishlikewater.client.handle;


import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @since: 2018年12月26日 10:52
 **/
@Slf4j
public class ProxyHttpMessageHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    //保留全局ctx
    private final ProxyClient client;

    public ProxyHttpMessageHandler(ProxyClient client) {
        this.client = client;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //log.info("连接活动");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ctx.close();
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) {
        switch (msg.getType()) {
            case REQUEST:
                Long requested = msg.getRequestId();
                MessageProbuf.Request request = msg.getRequest();
                String name = msg.getExtend();
                final ProxyConfig.HttpMapping httpMapping = ChannelKit.HTTP_MAPPING_MAP.get(name);
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(request.getHttpVersion()), HttpMethod.valueOf(request.getMethod()), request.getUrl());
                request.getHeaderMap().forEach((key, value) -> req.headers().set(key, value));
                req.headers().set("Host", (httpMapping.getAddress() + ":" + httpMapping.getPort()));
                req.content().writeBytes(request.getBody().toByteArray());
                Promise<Channel> promise = createPromise(httpMapping.getAddress(), httpMapping.getPort(), ctx);
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ToServerHandler(requested));
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
    private static Promise<Channel> createPromise(String host, int port, ChannelHandlerContext ctx) {
        Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new TempClientServiceInitializer());
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        promise.setSuccess(channelFuture.channel());
                    } else {
                        log.debug("connection fail address {}, port {}", host, port);
                        channelFuture.cancel(true);
                    }
                });
        return promise;
    }


}
