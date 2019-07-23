package com.github.fishlikewater.proxy.handler.p2p;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月23日 9:08
 * @since
 **/
public class P2pDecode extends MessageToMessageDecoder<DatagramPacket> {


    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {

    }
}
