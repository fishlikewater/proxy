package com.github.fishlikewater.callclient.handle;

import com.github.fishlikewater.callclient.config.ProxyConfig;
import com.github.fishlikewater.callclient.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.callclient.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.callclient.handle.socks.Socks5PasswordAuthRequestHandler;
import com.github.fishlikewater.config.ProxyType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fishlikewater@126.com
 * @since 2019年02月26日 21:47
 **/
@Slf4j
public class ProxyClientInitializer extends ChannelInitializer<Channel> {


    private final ProxyConfig proxyConfig;

    public ProxyClientInitializer(ProxyConfig proxyConfig) {
        log.info("init handler");
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        if (proxyConfig.isLogging()) {
            p.addLast(new LoggingHandler());
        }
        // socks5代理服务器
        else if (proxyConfig.getProxyType() == ProxyType.socks){
            p.addFirst(new Socks5CommandRequestDecoder());
            if (proxyConfig.isAuth()) {
                /* 添加验证机制*/
                p.addFirst(new Socks5PasswordAuthRequestHandler(proxyConfig));
                p.addFirst(new Socks5PasswordAuthRequestDecoder());
            }
            p.addFirst(new Socks5InitialAuthHandler(proxyConfig.isAuth()));
            p.addFirst(new ChunkedWriteHandler());
            p.addFirst(Socks5ServerEncoder.DEFAULT);
            p.addFirst(new Socks5InitialRequestDecoder());
            /* Socks connection handler */
            p.addLast(new Socks5CommandRequestHandler(proxyConfig.isMapping()));

        }


    }
}
