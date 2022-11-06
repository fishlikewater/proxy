package com.github.fishlikewater.proxyp2p.kit;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.DefaultAddressedEnvelope;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月05日 9:26
 **/
public class MessageKit {

    private static final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
            .setType(MessageProbuf.MessageType.MAKE_HOLE)
            .build();

    private static final MessageProbuf.Message ackMessage = MessageProbuf.Message.newBuilder()
            .setType(MessageProbuf.MessageType.ACK)
            .build();

    public static final MessageProbuf.Message closeMsg = MessageProbuf.Message.newBuilder()
            .setType(MessageProbuf.MessageType.CLOSE)
            .build();


    public static AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> getAckMsg(InetSocketAddress inetSocketAddress){
        return  new DefaultAddressedEnvelope<>(ackMessage, inetSocketAddress,
                new InetSocketAddress(0));
    }

    public static AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> getMakeHoleMsg(MessageProbuf.Socks scoks){
        return  new DefaultAddressedEnvelope<>(message, new InetSocketAddress(scoks.getAddress(), scoks.getPort()),
                new InetSocketAddress(0));
    }

    public static AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> getMakeHoleMsg(InetSocketAddress inetSocketAddress){
        return  new DefaultAddressedEnvelope<>(message, inetSocketAddress,
                new InetSocketAddress(0));
    }

    public static AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> getCloseMsg(InetSocketAddress inetSocketAddress){
        return  new DefaultAddressedEnvelope<>(closeMsg, inetSocketAddress,
                new InetSocketAddress(0));
    }

}
