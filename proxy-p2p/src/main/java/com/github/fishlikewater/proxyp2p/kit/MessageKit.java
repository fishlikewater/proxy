package com.github.fishlikewater.proxyp2p.kit;

import cn.hutool.core.util.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.ACK;
import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.MAKE_HOLE;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月05日 9:26
 **/
public class MessageKit {

    public static DatagramPacket getAckMsg(InetSocketAddress inetSocketAddress){
        MessageData ackMessage =new MessageData().setCmdEnum(ACK);
        final byte[] bytesMsg = ObjectUtil.serialize(ackMessage);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        buf.writeBytes(bytesMsg);
        return new DatagramPacket(buf, inetSocketAddress);
    }

    public static DatagramPacket getMakeHoleMsg(MessageData.Dst dst, InetSocketAddress sender){
        MessageData message = new MessageData().setCmdEnum(MAKE_HOLE);
        final byte[] bytesMsg = ObjectUtil.serialize(message);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        buf.writeBytes(bytesMsg);
        return new DatagramPacket(buf, new InetSocketAddress(dst.getDstAddress(), dst.getDstPort()), sender);
    }

    public static DatagramPacket getMakeHoleMsg(InetSocketAddress inetSocketAddress){
        MessageData message = new MessageData().setCmdEnum(MAKE_HOLE);
        final byte[] bytesMsg = ObjectUtil.serialize(message);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        buf.writeBytes(bytesMsg);
        return new DatagramPacket(buf, inetSocketAddress);
    }


    public static ByteBuf getByteBuf(ByteBuf byteBuf, MessageData.CmdEnum cmd, String requestId){
        final MessageData messageData = new MessageData();
        messageData.setByteBuf(byteBuf);
        messageData.setId(requestId);
        messageData.setCmdEnum(cmd);
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
