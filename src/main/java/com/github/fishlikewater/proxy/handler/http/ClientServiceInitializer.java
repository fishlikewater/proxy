package com.github.fishlikewater.proxy.handler.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;

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
    public ClientServiceInitializer(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        //SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        //SSLEngine sslEngine = sslCtx.newEngine(ch.alloc());
        ChannelPipeline p = ch.pipeline();
        //p.addLast("ssl", new SslHandler(sslEngine));
        p.addLast("http", new HttpClientCodec());
    }
}
