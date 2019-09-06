package com.github.fishlikewater.proxy.boot;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
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

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName NettyUdpServer
 * @Description
 * @Date 2019年03月06日 16:42
 * @since
 **/
@Slf4j
public class NettyUdpServer {

    private EventLoopGroup bossGroup;

    private ProxyConfig proxyConfig;

    public NettyUdpServer(ProxyConfig proxyConfig){
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
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new DatagramDnsQueryDecoder());
                        ch.pipeline().addLast(new DatagramDnsResponseEncoder());
                        ch.pipeline().addLast(new DnsServerHandler());
                    }
                });
        try {
            log.info("⬢ start {} server the port:{}",  proxyConfig.getType(), proxyConfig.getPort());
            bootstrap
                    .bind(proxyConfig.getPort())
                    .sync()
                    .channel()
                    .closeFuture().addListener(t->{
                        log.info("⬢ 停止{}服务", proxyConfig.getType());
            });
            DNSUtils.init(proxyConfig.getProxyDns());
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }

    }

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyConfig.getType());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            log.info("⬢ {} shutdown successful", proxyConfig.getType());
        } catch (Exception e) {
            log.error("⬢ proxyConfig.getType()"+" shutdown error", e);
        }
    }
}
