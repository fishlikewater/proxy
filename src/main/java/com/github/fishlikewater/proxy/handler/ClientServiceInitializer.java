package com.github.fishlikewater.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLEngine;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyServiceInitializer
 * @Description
 * @date 2019年02月26日 21:47
 **/
public class ClientServiceInitializer extends ChannelInitializer<Channel> {
    private ChannelHandlerContext ctx;
    private String host;
    private int port;
    public ClientServiceInitializer(ChannelHandlerContext ctx, String host, int port){
        this.ctx = ctx;
        this.host = host;
        this.port = port;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        SSLEngine sslEngine = sslCtx.newEngine(ch.alloc());
        ChannelPipeline p = ch.pipeline();
        p.addLast("none", new NoneHandler(ctx.channel()));
        p.addLast("ssl", new SslHandler(sslEngine));
    }
}
