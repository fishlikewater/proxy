package com.github.fishlikewater.cutpackets.boot;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月15日 5:58
 **/
@Slf4j
public class CutPacketsBoot {

    /**
     * 处理连接
     */
    @Setter
    @Getter
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    @Setter
    @Getter
    private EventLoopGroup workerGroup;


    protected void config(ServerBootstrap serverBootstrap) {
        if (EpollKit.epollIsAvailable()) {
            setBossGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("cut-epoll-boss@")));
            setWorkerGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("cut-epoll-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(EpollServerSocketChannel.class);
        } else {
            setBossGroup(new NioEventLoopGroup(0, new NamedThreadFactory("cut-nio-boss@")));
            setWorkerGroup(new NioEventLoopGroup(0, new NamedThreadFactory("cut-nio-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(NioServerSocketChannel.class);
        }
    }

    public void start() {
        final ServerBootstrap serverBootstrap = BootStrapFactory.getServerBootstrap();
        config(serverBootstrap);
        serverBootstrap.childHandler(new CutPacketsInitializer());
        try {
            Channel ch = serverBootstrap.bind("192.168.12.0/24", 0).addListener(future -> {
                if (future.isSuccess()) {

                }
            }).sync().channel();
            log.info("⬢ start server this port:{} and address:{} proxy type: socks5", "", "");
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
