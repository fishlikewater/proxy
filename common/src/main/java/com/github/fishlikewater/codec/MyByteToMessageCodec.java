package com.github.fishlikewater.codec;

import com.github.fishlikewater.kit.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 自定义消息编解码器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月18日 17:41
 **/
public class MyByteToMessageCodec extends ByteToMessageCodec<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) {
        if (out.isWritable()) {
            encode(msg, out);
        }
    }

    public void encode(MessageProtocol msg, ByteBuf out) {
        //占位
        out.writeInt(0);
        final byte[] bytes = KryoUtil.writeObjectToByteArray(msg);
        out.writeBytes(bytes);
        final int length = out.readableBytes();
        out.setInt(0, length - 4);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()) {
            final MessageProtocol messageProtocol = decode(in);
            if (Objects.nonNull(messageProtocol)) {
                out.add(messageProtocol);
            }
        }
    }

    public MessageProtocol decode(ByteBuf in) {
        in.readInt();
        final int readableBytes = in.readableBytes();
        if (readableBytes > 0) {
            final byte[] bytes = new byte[readableBytes];
            in.readBytes(bytes);
            return KryoUtil.readObjectFromByteArray(bytes, MessageProtocol.class);
        }
        return null;
    }
}
