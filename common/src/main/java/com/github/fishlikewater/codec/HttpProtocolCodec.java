package com.github.fishlikewater.codec;

import com.github.fishlikewater.kit.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月07日 11:05
 **/
public class HttpProtocolCodec extends ByteToMessageCodec<HttpProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpProtocol msg, ByteBuf out) {
        if (out.isWritable()){
            encode(msg, out);
        }
    }

    public void encode(HttpProtocol msg, ByteBuf out){
        //占位
        out.writeInt(0);
        final byte[] bytes = KryoUtil.writeObjectToByteArray(msg);
        out.writeBytes(bytes);
        final int length = out.readableBytes();
        out.setInt(0, length-4);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()){
            final HttpProtocol httpProtocol = decode(in);
            if (Objects.nonNull(httpProtocol)){
                out.add(httpProtocol);
            }
        }
    }

    public HttpProtocol decode(ByteBuf in){
        in.readInt();
        final int readableBytes = in.readableBytes();
        if (readableBytes > 0){
            final byte[] bytes = new byte[readableBytes];
            in.readBytes(bytes);
            return KryoUtil.readObjectFromByteArray(bytes, HttpProtocol.class);
        }
        return null;
    }


}
