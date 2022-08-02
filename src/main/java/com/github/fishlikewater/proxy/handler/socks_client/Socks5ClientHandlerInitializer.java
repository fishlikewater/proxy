package com.github.fishlikewater.proxy.handler.socks_client;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.*;
import lombok.RequiredArgsConstructor;

/**
 * @Description:
 * @date: 2022年07月18日 14:46
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@RequiredArgsConstructor
public class Socks5ClientHandlerInitializer extends ChannelInitializer<Channel> {

    private final ProxyConfig proxyConfig;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addFirst(new Socks5CommandResponseDecoder());
        p.addFirst(new Socks5CommandRequestDecoder());
        p.addFirst(new Socks5ClientPasswordAuthRequestHandler());
        p.addFirst(new Socks5ClientInitialAuthHandler(proxyConfig));
        p.addFirst(Socks5ClientEncoder.DEFAULT);
        p.addFirst(new Socks5PasswordAuthResponseDecoder());
        p.addFirst(new Socks5InitialResponseDecoder());  //1
        /** Socks connection handler */
        p.addLast(new Socks5ClientCommandRequestHandler());

    }
}
