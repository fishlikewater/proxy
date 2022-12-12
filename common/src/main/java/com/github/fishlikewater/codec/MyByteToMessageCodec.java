package com.github.fishlikewater.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *  自定义消息编解码器
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月18日 17:41
 **/
public class MyByteToMessageCodec extends ByteToMessageCodec<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) {
        if (out.isWritable()){
           encode(msg, out);
        }
    }

    public void encode(MessageProtocol msg, ByteBuf out){
        out.writeInt(0);
        out.writeByte(msg.getCmd().getCode());
        out.writeByte(msg.getProtocol().getCode());
        out.writeLong(msg.getId());
        out.writeByte(msg.getState());
        if (msg.getCmd() == MessageProtocol.CmdEnum.CONNECTION)
        {
            out.writeInt(msg.getDst().getDstPort());
            final String dstAddress = msg.getDst().getDstAddress();
            final byte[] bytes = dstAddress.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
        if (msg.getBytes() != null){
            out.writeBytes(msg.getBytes());

        }
        final int length = out.readableBytes();
        out.setInt(0, length-4);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()){
            final MessageProtocol messageProtocol = decode(in);
            out.add(messageProtocol);
        }
    }

    public MessageProtocol decode(ByteBuf in){
        in.readInt();
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setCmd(MessageProtocol.CmdEnum.getInstance(in.readByte()));
        messageProtocol.setProtocol(MessageProtocol.ProtocolEnum.getInstance(in.readByte()));
        messageProtocol.setId(in.readLong());
        messageProtocol.setState(in.readByte());
        if (messageProtocol.getCmd() == MessageProtocol.CmdEnum.CONNECTION)
        {
            final MessageProtocol.Dst dst = new MessageProtocol.Dst();
            dst.setDstPort(in.readInt());
            final int length = in.readInt();
            final ByteBuf byteBuf = in.readBytes(length);
            final String address = byteBuf.toString(StandardCharsets.UTF_8);
            dst.setDstAddress(address);
            messageProtocol.setDst(dst);
            byteBuf.release();
        }
        final int readableBytes = in.readableBytes();
        if (readableBytes > 0){
            final byte[] bytes = new byte[readableBytes];
            in.readBytes(bytes);
            messageProtocol.setBytes(bytes);
        }
        return messageProtocol;
    }
}
