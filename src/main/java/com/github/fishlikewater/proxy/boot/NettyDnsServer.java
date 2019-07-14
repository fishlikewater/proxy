package com.github.fishlikewater.proxy.boot;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.dns.DnsServerHandler;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName NettyDnsServer
 * @Description
 * @Date 2019年03月06日 16:42
 * @since
 **/
@Slf4j
public class NettyDnsServer {

    private EventLoopGroup bossGroup;

    private ProxyConfig proxyConfig;

    public NettyDnsServer(ProxyConfig proxyConfig){
        this.proxyConfig = proxyConfig;
    }

    public void start(){
        Bootstrap bootstrap = new Bootstrap();
        bossGroup = EpollKit.epollIsAvailable()?new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-boss@")):new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@"));
        Class clazz = EpollKit.epollIsAvailable()? EpollDatagramChannel.class:NioDatagramChannel.class;
        bootstrap
                .group(bossGroup)
                .option(ChannelOption.SO_BROADCAST, true)
                .channel(clazz)
                .handler(new DnsServerHandler());
        try {
            log.info("start dns server the port:{}", proxyConfig.getPort());
            bootstrap
                    .bind(proxyConfig.getPort())
                    .sync()
                    .channel()
                    .closeFuture().addListener(t->{
                        log.info("停止proxy dns服务");
            }).await();
        } catch (InterruptedException e) {
            log.error("start dns server fail", e);
            bossGroup.shutdownGracefully();
        }finally {
            bossGroup.shutdownGracefully();
        }

    }
}
