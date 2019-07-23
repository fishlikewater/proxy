package com.github.fishlikewater.proxy.handler.p2p.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月23日 9:01
 * @since
 **/
public class P2pClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

    }
}
