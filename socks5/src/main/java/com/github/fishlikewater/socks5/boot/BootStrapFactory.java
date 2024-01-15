package com.github.fishlikewater.socks5.boot;

import com.github.fishlikewater.kit.EpollKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月15日 15:50
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BootStrapFactory {

    private static Bootstrap bootstrap;
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static ServerBootstrap getServerBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    public static Bootstrap getBootstrap(ChannelHandlerContext ctx) {
        if (Objects.nonNull(bootstrap)) {
            return bootstrap;
        }
        try {
            LOCK.lock();
            if (Objects.nonNull(bootstrap)) {
                return bootstrap;
            }
            bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop().parent());
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
            if (EpollKit.epollIsAvailable()) {
                bootstrap.channel(EpollSocketChannel.class);
            } else {
                bootstrap.channel(NioSocketChannel.class);
            }
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    log.info("socks empty handler");
                }
            });
            return bootstrap;

        } finally {
            LOCK.unlock();
        }

    }
}
