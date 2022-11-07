package com.github.fishlikewater.proxyp2p.kit;

import cn.hutool.core.util.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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

    public static AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> getMakeHoleMsg(MessageData.Dst dst){
        return  new DefaultAddressedEnvelope<>(message, new InetSocketAddress(dst.getDstAddress(), dst.getDstPort()),
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

    public static ByteBuf getByteBuf(ByteBuf byteBuf, MessageData.CmdEnum cmd, String requestId){
        final MessageData messageData = new MessageData();
        messageData.setByteBuf(byteBuf);
        messageData.setId(requestId);
        messageData.setCmdEnum(MessageData.CmdEnum.CONNECTION);
        final byte[] bytesMsg = ObjectUtil.serialize(messageData);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        buf.writeBytes(bytesMsg);
        return buf;
    }

    public static ByteBuf getByteBuf(MessageData messageData){
        final byte[] bytesMsg = ObjectUtil.serialize(messageData);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        buf.writeBytes(bytesMsg);
        return buf;
    }

}
