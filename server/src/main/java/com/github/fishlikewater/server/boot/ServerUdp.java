package com.github.fishlikewater.server.boot;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * udp 中间桥接服务
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年08月26日 10:34
 **/
@Slf4j
@RequiredArgsConstructor
public class ServerUdp {


    private final String address;

    private final int port;

    private EventLoopGroup workGroup;
    private Bootstrap bootstrap;

    void bootstrapConfig() {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            if (EpollKit.epollIsAvailable()) {
                workGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("udp-epoll-work@"));
                bootstrap.group(workGroup).channel(EpollDatagramChannel.class);
            } else {
                workGroup = new NioEventLoopGroup(0, new NamedThreadFactory("udp-nio-work@"));
                bootstrap.group(workGroup).channel(NioDatagramChannel.class);
            }
            //bootstrap.handler(new ClientHandlerInitializer(proxyConfig, this));
        }
    }

    public void start() {
        try {
            bootstrapConfig();
            bootstrap.bind(address, port).sync();
        } catch (InterruptedException e) {
            log.error("udp启动失败", e);
            stop();
        }
    }

    public void stop() {
        try {
            if (this.workGroup != null) {
                this.workGroup.shutdownGracefully().addListener(f -> {

                });
            }
            log.info("⬢ udp shutdown successful");
        } catch (Exception e) {
            log.error("⬢ udp shutdown error");
        }
    }

}