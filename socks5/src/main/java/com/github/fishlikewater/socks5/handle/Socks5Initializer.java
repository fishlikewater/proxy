package com.github.fishlikewater.socks5.handle;

import com.github.fishlikewater.socks5.config.Socks5Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fishlikewater@126.com
 * @since 2019年02月26日 21:47
 **/
@Slf4j
public class Socks5Initializer extends ChannelInitializer<Channel> {


    private final Socks5Config socks5Config;
    private final Channel channelClient;

    public Socks5Initializer(Socks5Config socks5Config, Channel channelClient) {
        log.info("init handler");
        this.socks5Config = socks5Config;
        this.channelClient = channelClient;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addFirst(new Socks5CommandRequestDecoder());
        if (socks5Config.isAuth()) {
            /* 添加验证机制*/
            p.addFirst(new Socks5PasswordAuthRequestHandler(socks5Config));
            p.addFirst(new Socks5PasswordAuthRequestDecoder());
        }
        p.addFirst(new Socks5InitialAuthHandler(socks5Config.isAuth()));
        p.addFirst(new ChunkedWriteHandler());
        p.addFirst(Socks5ServerEncoder.DEFAULT);
        p.addFirst(new Socks5InitialRequestDecoder());
        /* Socks connection handler */
        p.addLast(new Socks5CommandRequestHandler(channelClient, socks5Config.isCheckConnect()));


    }
}
