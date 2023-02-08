package com.github.fishlikewater.client.handle;


import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.HttpProtocol;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @since: 2018年12月26日 10:52
 **/
@Slf4j
public class ProxyHttpMessageHandler extends SimpleChannelInboundHandler<HttpProtocol> {

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
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpProtocol msg) {
        switch (msg.getCmd()) {
            case REQUEST:
                Long requested = msg.getId();
                final byte[] bytes = msg.getBytes();
                String name = msg.getDstServer();
                String url = msg.getUrl();
                final ProxyConfig.HttpMapping httpMapping = ChannelKit.HTTP_MAPPING_MAP.get(name);
                if (httpMapping.isDelNameWithPath()){
                    url = url.replace("/" + httpMapping.getName(), "");
                }
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(msg.getVersion()), HttpMethod.valueOf(msg.getMethod()), url);
                req.headers().add(msg.getHeads());
                req.headers().set("Host", (httpMapping.getAddress() + ":" + httpMapping.getPort()));
                req.content().writeBytes(msg.getBytes());

                Promise<Channel> promise = BootStrapFactory.createPromise(httpMapping.getAddress(), httpMapping.getPort(), ctx);
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ToServerHandler(requested));
                        channelFuture.get().writeAndFlush(req);
                    }
                });

                break;
            case AUTH:
                String authMsg = new String(msg.getBytes(), StandardCharsets.UTF_8);
                log.info(authMsg);
                break;
            case HEALTH:
                log.info("get receipt health packet from server");
                break;
            case CLOSE:
                ctx.channel().close();
            default:
                log.info("noknow message");
                break;
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
