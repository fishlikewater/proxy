package com.github.fishlikewater.proxy.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName NoneHandler
 * @Description
 * @date 2019年02月26日 21:51
 **/
public class NoneHandler extends ChannelInboundHandlerAdapter {

    private Channel outChannel;

    public NoneHandler(Channel outChannel) {
        this.outChannel = outChannel;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("交换数据");
        outChannel.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        outChannel.flush();
    }
}
