package com.github.fishlikewater.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月23日 14:52
 **/
public class StatisticsHandler extends ChannelDuplexHandler {

    private final boolean InData;
    private final boolean outData;

    public StatisticsHandler(boolean inData, boolean outData){

        this.InData = inData;
        this.outData = outData;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (InData){
            System.out.println("入站数据: " + ((ByteBuf)msg).readableBytes());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (outData){
            System.out.println("出站数据: " + ((ByteBuf)msg).readableBytes());
        }
        super.write(ctx, msg, promise);
    }
}
