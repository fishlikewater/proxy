package com.github.fishlikewater.socks5.handle;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>
 * 对socks请求本地发起请求
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年08月08日 12:23
 **/
@Slf4j
@RequiredArgsConstructor
public class Client2LocalHandler extends ChannelInboundHandlerAdapter {

    private final Channel channel;

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.debug(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        channel.config().setAutoRead(canWrite);
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("将客户端的消息转发给目标服务器端");
        channel.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("客户端断开连接");
        channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }


}
