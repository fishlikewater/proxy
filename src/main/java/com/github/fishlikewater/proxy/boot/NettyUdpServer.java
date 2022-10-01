package com.github.fishlikewater.proxy.boot;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import com.github.fishlikewater.proxy.handler.dns.DNSUtils;
import com.github.fishlikewater.proxy.handler.dns.DnsServerHandler;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName NettyUdpServer
 * @Description
 * @since 2019年03月06日 16:42
 **/
@Slf4j
public class NettyUdpServer implements DisposableBean {

    private EventLoopGroup bossGroup;

    private final ProxyConfig proxyConfig;

    private final ProxyType proxyType;

    public NettyUdpServer(ProxyConfig proxyConfig,  ProxyType proxyType){
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
    }

    public void start(){
        Bootstrap bootstrap = new Bootstrap();
        bossGroup = EpollKit.epollIsAvailable()?new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-boss@")):new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@"));
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bootstrap.channel(EpollDatagramChannel.class);
        }else {
            bootstrap.channel(NioDatagramChannel.class);
        }
        bootstrap
                .group(bossGroup)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    protected void initChannel(NioDatagramChannel ch) {
                        ch.pipeline().addLast(new DatagramDnsQueryDecoder());
                        ch.pipeline().addLast(new DatagramDnsResponseEncoder());
                        ch.pipeline().addLast(new DnsServerHandler());
                    }
                });
        try {
            log.info("⬢ start {} server the port:{}",  proxyType, proxyConfig.getPort());
            bootstrap
                    .bind(proxyConfig.getPort())
                    .sync()
                    .channel()
                    .closeFuture().addListener(t-> log.info("⬢ 停止{}服务", proxyType));
            DNSUtils.init(proxyConfig.getProxyDns());
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }

    }

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyType);
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            log.info("⬢ {} shutdown successful", proxyType);
        } catch (Exception e) {
            log.error("⬢ proxyConfig.getType()"+" shutdown error", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}
