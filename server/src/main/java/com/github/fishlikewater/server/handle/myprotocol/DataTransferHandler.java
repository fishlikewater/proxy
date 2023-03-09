package com.github.fishlikewater.server.handle.myprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static com.github.fishlikewater.server.kit.ChannelGroupKit.DATA_CHANNEL;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月22日 21:34
 **/
public class DataTransferHandler extends SimpleChannelInboundHandler<byte[]> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        final Channel channel = ctx.channel().attr(DATA_CHANNEL).get();
        if (channel != null){
            channel.writeAndFlush(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel().attr(DATA_CHANNEL).get();
        if (channel != null){
            channel.close();
        }
        super.channelInactive(ctx);
    }
}
