package com.github.fishlikewater.proxyp2p.call;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import io.netty.bootstrap.ServerBootstrap;
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
 *     启动类
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月18日 16:06
 **/
@Slf4j
public abstract class Server {

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

    @Setter
    @Getter
    private CallConfig callConfig;

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", "socks");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ shutdown {} successful", "socks");
        } catch (Exception e) {
            log.error("⬢ {} shutdown error","socks", e);
        }
    }


    protected void config(ServerBootstrap serverBootstrap){
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            setBossGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-boss@")));
            setWorkerGroup(new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(EpollServerSocketChannel.class);
        } else {
            setBossGroup(new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@")));
            setWorkerGroup(new NioEventLoopGroup(0, new NamedThreadFactory("nio-worker@")));
            serverBootstrap.group(getBossGroup(), getWorkerGroup()).channel(NioServerSocketChannel.class);
        }
    }



}
