package com.github.fishlikewater.server.handle;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName NoneHandler
 * @Description
 * @date 2019年02月26日 21:51
 **/
@Slf4j
public class NoneHandler extends SimpleChannelInboundHandler<Object> {

    private final Channel outChannel;

    public NoneHandler(Channel outChannel) {
        this.outChannel = outChannel;
    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.debug(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        outChannel.config().setAutoRead(canWrite);
        super.channelWritabilityChanged(ctx);
    }

    /**
     *  关闭远程目标连接
     * @param: ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outChannel != null && outChannel.isActive()) {
            outChannel.writeAndFlush(PooledByteBufAllocator.DEFAULT.buffer()).addListener(future -> outChannel.close().addListener(future1 -> log.debug("返回0字节：browser关闭连接，因此关闭到webserver连接")));
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        ReferenceCountUtil.retain(msg);
        outChannel.writeAndFlush(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
