package com.github.fishlikewater.proxyp2p.server.handle;

import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月29日 14:44
 **/
public class UdpP2pDataHandler extends SimpleChannelInboundHandler<ProbufData> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) throws Exception {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        System.out.println(msgMessage);
        if (msgMessage.getType() == MessageProbuf.MessageType.MAKE_HOLE_INIT){
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setType(MessageProbuf.MessageType.MAKE_HOLE_INIT)
                    .setLength(100)
                    .build();
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(message, msg.getSender(), new InetSocketAddress(8088));
            ctx.writeAndFlush(addressedEnvelope);
        }
    }
}
