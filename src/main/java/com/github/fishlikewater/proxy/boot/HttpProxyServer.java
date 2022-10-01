package com.github.fishlikewater.proxy.boot;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import com.github.fishlikewater.proxy.handler.ProxyServiceInitializer;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

/**
 * @Description:
 * @since: 2022/10/1 9:52
 */
@Slf4j
public class HttpProxyServer implements DisposableBean {
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    private EventLoopGroup workerGroup;

    private final ProxyConfig proxyConfig;

    private final ProxyType proxyType;

    public HttpProxyServer(ProxyConfig proxyConfig, ProxyType proxyType) {
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-boss@"));
            workerGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-worker@"));
            bootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@"));
            workerGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-worker@"));
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        }
        bootstrap.childHandler(new ProxyServiceInitializer(proxyConfig, ProxyType.http));
        try {
            Channel ch;
            if (proxyConfig.getAddress() == null) {
                ch = bootstrap.bind(proxyConfig.getPort()).sync().channel();
            } else {
                ch = bootstrap.bind(proxyConfig.getAddress(), proxyConfig.getHttpPort()).sync().channel();
                log.info("⬢ start {} server this port:{} and adress:{} proxy type:{}", proxyType, proxyConfig.getHttpPort(), proxyConfig.getAddress(), proxyType);
            }

            ch.closeFuture().addListener(t -> log.info("⬢  {}服务开始关闭", proxyType));
        } catch (InterruptedException e) {
            log.error("⬢ start {} server fail", proxyType, e);
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} server shutdown ...", proxyType);
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ {} shutdown successful", proxyType);
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", proxyType, e);
        }
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}
