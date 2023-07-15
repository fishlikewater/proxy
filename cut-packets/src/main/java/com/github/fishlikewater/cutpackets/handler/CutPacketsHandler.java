package com.github.fishlikewater.cutpackets.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月15日 6:05
 **/
public class CutPacketsHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
        System.out.println(remoteAddress);
    }
}
