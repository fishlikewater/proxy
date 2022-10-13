package com.github.fishlikewater.server.kit;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.server.boot.NoneClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月15日 15:50
 **/
public class BootStrapFactroy {

    private static Bootstrap bootstrap = null;

    public static Bootstrap bootstrapConfig(ChannelHandlerContext ctx){
        if(bootstrap != null){
            return bootstrap.clone();
        }
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.group(ctx.channel().eventLoop().parent());
        bootstrap.handler(new NoneClientInitializer());
        return bootstrap;
    }
}
