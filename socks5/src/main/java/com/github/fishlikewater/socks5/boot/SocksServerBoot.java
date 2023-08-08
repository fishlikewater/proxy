package com.github.fishlikewater.socks5.boot;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.socks5.config.Socks5Config;
import com.github.fishlikewater.socks5.handle.Socks5Initializer;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  socks 启动类
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年10月18日 16:01
 **/
@Getter
@Slf4j
@RequiredArgsConstructor
public class SocksServerBoot{

    /**
     * 处理连接
     */
    @Setter
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    @Setter
    @Getter
    private EventLoopGroup workerGroup;

    private final Socks5Config socks5Config;



    protected void config(ServerBootstrap serverBootstrap){
        if (EpollKit.epollIsAvailable()) {
            setBossGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("socks5-epoll-boss@")));
            setWorkerGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("socks5-epoll-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(EpollServerSocketChannel.class);
        } else {
            setBossGroup(new NioEventLoopGroup(0, new NamedThreadFactory("socks5-nio-boss@")));
            setWorkerGroup(new NioEventLoopGroup(0, new NamedThreadFactory("socks5-nio-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(NioServerSocketChannel.class);
        }
    }

    public void start() {
        final ServerBootstrap serverBootstrap = BootStrapFactory.getServerBootstrap();
        config(serverBootstrap);
        serverBootstrap.childHandler(new Socks5Initializer(socks5Config));
        try {
            Channel ch = serverBootstrap.bind(socks5Config.getAddress(), socks5Config.getPort()).addListener(future -> {
                if (future.isSuccess()){
                    Socks5Kit.channel.attr(Socks5Kit.CHANNELS_SOCKS).set(new ConcurrentHashMap<>(16));
                }
            }).sync().channel();
            log.info("⬢ start server this port:{} and address:{} proxy type: socks5",socks5Config.getPort(), socks5Config.getAddress());
            ch.closeFuture().addListener(t -> log.info("⬢  socks5服务开始关闭"));
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ shutdown socks5 successful");
        } catch (Exception e) {
            log.error("⬢ socks5 shutdown error", e);
        }
    }

}
