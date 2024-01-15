package com.github.fishlikewater.client.handle;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
@Slf4j
public class NoneClientInitializer extends ChannelInitializer<Channel> {

    private final boolean ssl;

    public NoneClientInitializer(boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    protected void initChannel(Channel ch) throws SSLException {
        if (ssl) {
            try {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addFirst(sslContext.newHandler(ByteBufAllocator.DEFAULT.buffer().alloc()));
            } catch (SSLException e) {
                log.error("初始化ssl异常", e);
                throw e;
            }
        }
    }
}
