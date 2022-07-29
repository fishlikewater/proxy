package com.github.fishlikewater.proxy.handler.socks_proxy;

import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description:
 * @date: 2022年07月29日 14:18
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
public class Client2DestHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        msg.getResponse().getBody();
    }
}
