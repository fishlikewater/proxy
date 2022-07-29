package com.github.fishlikewater.proxy.handler.socks_proxy;

import com.github.fishlikewater.proxy.handler.BootStrapFactroy;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @date: 2022年07月29日 14:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
public class Dest2ClientHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {

    }
}
