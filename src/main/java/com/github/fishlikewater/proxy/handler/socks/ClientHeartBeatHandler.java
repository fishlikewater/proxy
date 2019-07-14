package com.github.fishlikewater.proxy.handler.socks;

import com.github.fishlikewater.proxy.kit.BootClientPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            BootClientPool.single().remove(remoteAddress.getHostString()+":"+remoteAddress.getPort());
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
