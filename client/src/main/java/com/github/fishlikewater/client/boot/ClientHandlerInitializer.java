package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ClientHeartBeatHandler;
import com.github.fishlikewater.client.handle.ClientMessageHandler;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2018年12月25日 15:05
 **/
public class ClientHandlerInitializer extends ChannelInitializer<Channel> {

    private final ProxyConfig proxyConfig;

    private final ProxyClient client;


    public ClientHandlerInitializer(ProxyConfig proxyConfig, ProxyClient client) {
        this.proxyConfig = proxyConfig;
        this.client = client;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        /* 是否打开日志*/
        if (proxyConfig != null && proxyConfig.isLogging()) {
            pipeline.addLast(new LoggingHandler());
        }

        assert proxyConfig != null;
        pipeline
                .addLast(new LengthFieldBasedFrameDecoder(5 * 1024 * 1024, 0, 4))
                .addLast(new MyByteToMessageCodec())
                .addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS))
                .addLast(new ClientHeartBeatHandler())
                .addLast(new ClientMessageHandler(client));


    }
}
