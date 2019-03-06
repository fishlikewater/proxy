package com.github.fishlikewater.proxy.handler;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import com.github.fishlikewater.proxy.handler.http.HttpServiceHandler;
import com.github.fishlikewater.proxy.handler.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.proxy.handler.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.proxy.handler.socks.Socks5PasswordAuthRequestHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyServiceInitializer
 * @Description
 * @date 2019年02月26日 21:47
 **/
public class ProxyServiceInitializer extends ChannelInitializer<Channel> {

/*
    //流量统计
    private static final EventExecutorGroup EXECUTOR_GROUOP = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    TrafficCounter trafficCounter = trafficHandler.trafficCounter();
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final long totalRead = trafficCounter.cumulativeReadBytes();
                    final long totalWrite = trafficCounter.cumulativeWrittenBytes();
                    System.out.println("total read: " + (totalRead >> 10) + " KB");
                    System.out.println("total write: " + (totalWrite >> 10) + " KB");
                    System.out.println("流量监控: " + System.lineSeparator() + trafficCounter);
                }
            }
        }).start();
    }
    private static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(EXECUTOR_GROUOP);
*/


    private ProxyConfig proxyConfig;

    public ProxyServiceInitializer(ProxyConfig proxyConfig){
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS));
        p.addLast(new HeartBeatHandler());
        if(proxyConfig.isLogging()){
            p.addLast(new LoggingHandler());
        }
        if(proxyConfig.getType() == ProxyType.http){
            p.addLast("httpcode", new HttpServerCodec());
            p.addLast("httpservice", new HttpServiceHandler(proxyConfig.isAuth()));
        }else{
            p.addLast(new Socks5InitialRequestDecoder());
            p.addLast(Socks5ServerEncoder.DEFAULT);
            p.addLast(new Socks5InitialAuthHandler(proxyConfig.isAuth()));
            if(proxyConfig.isAuth()) {
                //socks auth
                p.addLast(new Socks5PasswordAuthRequestDecoder());
                p.addLast(new Socks5PasswordAuthRequestHandler(proxyConfig));
            }
            //socks connection
            p.addLast(new Socks5CommandRequestDecoder());
            //Socks connection
            p.addLast(new Socks5CommandRequestHandler());

        }


    }
}
