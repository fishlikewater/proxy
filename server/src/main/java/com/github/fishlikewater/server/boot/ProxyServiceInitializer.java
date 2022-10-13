package com.github.fishlikewater.server.boot;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.*;
import com.github.fishlikewater.server.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.server.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.server.socks.Socks5PasswordAuthRequestHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyServiceInitializer
 * @Description
 * @date 2019年02月26日 21:47
 **/
@Slf4j
public class ProxyServiceInitializer extends ChannelInitializer<Channel> {

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
    private final ProxyType proxyType;
    private GlobalTrafficShapingHandler trafficHandler;

    public ProxyServiceInitializer(ProxyConfig proxyConfig, ProxyType proxyType) {
        log.info("init handler");
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        if (proxyConfig.getIsOpenGlobalTrafficLimit()) {
            if (trafficHandler == null) {
                trafficHandler = new GlobalTrafficShapingHandler(channel.eventLoop().parent(), proxyConfig.getWriteLimit(), proxyConfig.getReadLimit());
                log.info("开始全局流量监控");
                thread.start();
            }
            p.addLast("globallimit", trafficHandler);
        }
        p.addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS));
        /* http服务端 无心跳直接关闭*/
        if (proxyType == ProxyType.proxy_server_http || proxyType == ProxyType.http) {
            p.addLast(new HttpHeartBeatHandler());
        } else {
            /* 其他模式 发送心跳包到客户端确认*/
            p.addLast(new ServerHeartBeatHandler());
        }
        /* 是否打开日志*/
        if (proxyConfig.isLogging()) {
            p.addLast(new LoggingHandler());
        }
        /* http代理服务器*/
        if (proxyType == ProxyType.http) {
            p.addFirst("aggregator", new HttpObjectAggregator(1024*1024*100));
            p.addFirst("httpcode", new HttpServerCodec());
            p.addLast("httpservice", new HttpServiceHandler(proxyConfig.isAuth()));
        }
        /* http转发服务器(内网穿透)*/
        else if (proxyType == ProxyType.proxy_server_http) {
            p.addFirst("aggregator", new HttpObjectAggregator(1024*1024*100));
            p.addFirst("httpcode", new HttpServerCodec());
            p.addLast("proxyHttpServerHandler", new ProxyHttpServerHandler());
        }
        /* http协议转发(内网穿透)*/
        else if (proxyType == ProxyType.proxy_server) {
            p.addFirst(new ProtobufEncoder());
            p.addFirst(new ProtobufVarint32LengthFieldPrepender());
            p.addFirst(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()));
            p.addFirst(new ProtobufVarint32FrameDecoder());
            p.addLast("proxyProtobufServerHandler", new ProxyProtobufServerHandler(new DefaultConnectionValidate(), proxyConfig));
        }
        /* sockes5代理服务器*/
        else if (proxyType == ProxyType.socks){
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
