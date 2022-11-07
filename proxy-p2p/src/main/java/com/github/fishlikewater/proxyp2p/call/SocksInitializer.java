package com.github.fishlikewater.proxyp2p.call;

import com.github.fishlikewater.proxyp2p.call.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.proxyp2p.call.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhangx
 * @since: 2019年02月26日 21:47
 **/
@Slf4j
public class SocksInitializer extends ChannelInitializer<Channel> {

    private final CallConfig callConfig;

    public SocksInitializer(CallConfig callConfig) {
        log.info("init handler");
        this.callConfig = callConfig;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        /* socks connectionddecode */
        p.addFirst(new Socks5CommandRequestDecoder()); //7
        p.addFirst(new Socks5InitialAuthHandler(callConfig.isAuth())); //3
        p.addFirst(Socks5ServerEncoder.DEFAULT); //2
        p.addFirst(new Socks5InitialRequestDecoder());  //1
        /* Socks connection handler */
        p.addLast(new Socks5CommandRequestHandler());

    }
}
