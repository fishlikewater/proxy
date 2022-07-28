package com.github.fishlikewater.proxy.handler.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @Description:
 * @date: 2022年07月28日 15:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
public class ServiceHandler extends SimpleChannelInboundHandler<Object> {

    private final Channel channel;

    public ServiceHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 获取读取的数据， 是一个缓冲。
        ByteBuf readBuffer = (ByteBuf) msg;
        System.out.println("get data: " + readBuffer.toString(CharsetUtil.UTF_8));
        //这里的复位不能省略,不然会因为计数器问题报错.
        readBuffer.retain();
        //将数据发到远程客户端那边
        channel.writeAndFlush(readBuffer);
    }
}
