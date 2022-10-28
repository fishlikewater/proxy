package com.github.fishlikewater.server.handle;

import com.github.fishlikewater.server.handle.socks.Socks5Contans;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.AtomicLong;

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
            flow(ctx, (ByteBuf) msg);
        }
        super.channelRead(ctx, msg);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (outData){
            flow(ctx, (ByteBuf) msg);
        }
        super.write(ctx, msg, promise);
    }

    private void flow(ChannelHandlerContext ctx, ByteBuf msg) {
        final String account = ctx.attr(Socks5Contans.ACCOUNT).get();
        if (account != null){
            final AtomicLong atomicLong = Socks5Contans.accountFlow.get(account);
            if (atomicLong != null){
                atomicLong.addAndGet(msg.readableBytes());
            }else {
                final AtomicLong atomicLong1 = new AtomicLong(msg.readableBytes());
                Socks5Contans.accountFlow.put(account, atomicLong1);
            }
        }
    }
}
