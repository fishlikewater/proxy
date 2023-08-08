package com.github.fishlikewater.socks5.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年08月08日 12:31
 **/
@Slf4j
public class Local2ClientHandler extends SimpleChannelInboundHandler<Object> {


    private final ChannelHandlerContext clientChannelContext;

    public Local2ClientHandler(ChannelHandlerContext clientChannelContext) {
        this.clientChannelContext = clientChannelContext;
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.debug(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        clientChannelContext.channel().config().setAutoRead(canWrite);
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx2, Object destMsg) {
        log.debug("将目标服务器信息转发给客户端");
        clientChannelContext.writeAndFlush(destMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx2) {
        log.debug("目标服务器断开连接");
        clientChannelContext.channel().close();
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
