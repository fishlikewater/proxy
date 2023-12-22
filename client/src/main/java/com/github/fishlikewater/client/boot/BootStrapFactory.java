package com.github.fishlikewater.client.boot;

import com.github.fishlikewater.kit.EpollKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月15日 15:50
 **/
@Slf4j
public class BootStrapFactory {

    private static Bootstrap bootstrap = null;

    public static Bootstrap bootstrapConfig(ChannelHandlerContext ctx) {
        if (bootstrap != null) {
            return bootstrap;
        }
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        if (EpollKit.epollIsAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.group(ctx.channel().eventLoop().parent());
        return bootstrap;
    }

}
