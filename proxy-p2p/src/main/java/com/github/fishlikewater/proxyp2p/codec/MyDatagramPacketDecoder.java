package com.github.fishlikewater.proxyp2p.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月30日 11:56
 **/
public class MyDatagramPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private final MyProtobufDecoder decoder;

    /**
     * Create a {@link DatagramPacket} decoder using the specified {@link ByteBuf} decoder.
     *
     * @param decoder the specified {@link ByteBuf} decoder
     */
    public MyDatagramPacketDecoder(MyProtobufDecoder decoder) {
        this.decoder = checkNotNull(decoder, "decoder");
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            return decoder.acceptInboundMessage((DatagramPacket) msg);
        }
        return false;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        decoder.decode(ctx, msg, out);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        decoder.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        decoder.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        decoder.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        decoder.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        decoder.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        decoder.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        decoder.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        decoder.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        decoder.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        decoder.handlerRemoved(ctx);
    }

    @Override
    public boolean isSharable() {
        return decoder.isSharable();
    }
}
