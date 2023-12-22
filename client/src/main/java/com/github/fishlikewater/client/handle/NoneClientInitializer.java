package com.github.fishlikewater.client.handle;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
public class NoneClientInitializer extends ChannelInitializer<Channel> {

    private final boolean ssl;

    public NoneClientInitializer(boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    protected void initChannel(Channel ch) {
        if (ssl) {
            try {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addFirst(sslContext.newHandler(ByteBufAllocator.DEFAULT.buffer().alloc()));
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
