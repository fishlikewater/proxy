package com.github.fishlikewater.proxy.handler.p2p;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月23日 9:08
 * @since
 **/
public class P2pEncode extends MessageToMessageEncoder<P2pMessage> {


    @Override
    protected void encode(ChannelHandlerContext ctx, P2pMessage msg, List<Object> out) throws Exception {

        byte[] content = JSON.toJSONString(msg).getBytes(CharsetUtil.UTF_8);
        int length =content.length;
        ByteBufAllocator alloc = ctx.alloc();
        ByteBuf buffer = alloc.buffer(length);
        buffer.writeBytes(content);
        out.add(new DatagramPacket(buffer, (InetSocketAddress)ctx.channel().remoteAddress()));

    }

}