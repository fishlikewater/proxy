package com.github.fishlikewater.callclient.boot;

import com.github.fishlikewater.callclient.config.ProxyConfig;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
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
 * @author fishlikewater@126.com
 * @since 2022年10月18日 16:06
 **/
@Slf4j
public abstract class AbstractServer {

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
    private ProxyConfig proxyConfig;

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyConfig.getProxyType());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ shutdown {} successful", proxyConfig.getProxyType());
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", proxyConfig.getProxyType(), e);
        }
    }


    protected void config(ServerBootstrap serverBootstrap){
        if (EpollKit.epollIsAvailable()) {
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
