package com.github.fishlikewater.callcleint.handle;

import com.github.fishlikewater.callcleint.config.ProxyConfig;
import com.github.fishlikewater.callcleint.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.callcleint.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.callcleint.handle.socks.Socks5PasswordAuthRequestHandler;
import com.github.fishlikewater.config.ProxyType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author: zhangx
 * @date: 2019年02月26日 21:47
 **/
@Slf4j
public class ProxyClientInitializer extends ChannelInitializer<Channel> {

    private final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                TrafficCounter trafficCounter = trafficHandler.trafficCounter();
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final long totalRead = trafficCounter.cumulativeReadBytes();
                final long totalWrite = trafficCounter.cumulativeWrittenBytes();
                System.out.println("total read: " + (totalRead >> 10) + " KB");
                System.out.println("total write: " + (totalWrite >> 10) + " KB");
            }
        }
    });


    private final ProxyConfig proxyConfig;
    private GlobalTrafficShapingHandler trafficHandler;

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
        /* sockes5代理服务器*/
        else if (proxyConfig.getProxyType() == ProxyType.socks){
            /* socks connectionddecode */
            p.addFirst(new Socks5CommandRequestDecoder()); //7
            if (proxyConfig.isAuth()) {
                /* 添加验证机制*/
                p.addFirst(new Socks5PasswordAuthRequestHandler(proxyConfig)); //5
                p.addFirst(new Socks5PasswordAuthRequestDecoder()); //4
            }
            p.addFirst(new Socks5InitialAuthHandler(proxyConfig.isAuth())); //3
            p.addFirst(Socks5ServerEncoder.DEFAULT); //2
            p.addFirst(new Socks5InitialRequestDecoder());  //1
            /* Socks connection handler */
            p.addLast(new Socks5CommandRequestHandler());

        }


    }
}
