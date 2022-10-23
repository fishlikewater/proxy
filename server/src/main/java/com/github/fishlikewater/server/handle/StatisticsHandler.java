package com.github.fishlikewater.server.handle;

import com.github.fishlikewater.server.socks.Socks5Contans;
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
            final String account = ctx.attr(Socks5Contans.ACCOUNT).get();
            if (account != null){
                final AtomicLong atomicLong = Socks5Contans.accountFlow.get(account);
                if (atomicLong != null){
                    atomicLong.addAndGet(((ByteBuf)msg).readableBytes());
                    System.out.println("入站总数据: " + atomicLong.get());
                }else {
                    final AtomicLong atomicLong1 = new AtomicLong(((ByteBuf) msg).readableBytes());
                    Socks5Contans.accountFlow.put(account, atomicLong1);
                }
            }
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
