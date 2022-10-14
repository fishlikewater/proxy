package com.github.fishlikewater.callcleint.boot;

import com.github.fishlikewater.callcleint.config.ProxyConfig;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
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
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName TcpProxyServer
 * @date 2019年02月26日 21:45
 **/
@Slf4j
public class TcpServer implements DisposableBean {
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    private EventLoopGroup workerGroup;

    private final ProxyConfig proxyConfig;
    private final ProxyConfig.Mapping mapping;


    public TcpServer(ProxyConfig proxyConfig, ProxyConfig.Mapping mapping) {
        this.proxyConfig = proxyConfig;
        this.mapping = mapping;
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
        bootstrap.childHandler(new ClientHandlerInitializer(proxyConfig, ProxyType.tcp_server, mapping));
        try {
            Channel ch = bootstrap.bind(mapping.getLocalAddress(), mapping.getLocalPort()).sync().channel();

            log.info("⬢ start server this port:{} and adress:{} proxy type:{}", mapping.getLocalAddress(), mapping.getLocalPort(), ProxyType.tcp_server);
            ch.closeFuture().addListener(t -> log.info("⬢  {}服务开始关闭", ProxyType.tcp_server));
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", ProxyType.tcp_server);
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ {} shutdown successful", ProxyType.tcp_server);
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", ProxyType.tcp_server, e);
        }
    }

    /**
     * 数据交换连接客户端
     */
    private Bootstrap getBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        if (EpollKit.epollIsAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.group(bossGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return bootstrap;

    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}
