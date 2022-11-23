package com.github.fishlikewater.callcleint.handle;


import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangx
 * @version V1.0
 * @since: 2018年12月26日 10:52
 **/
@Slf4j
public class ClientDataHandler extends SimpleChannelInboundHandler<MessageProtocol> {


    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("数据通道连接断开");
        ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().clear();
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        MessageProtocol.CmdEnum type = msg.getCmd();
        final Long requestId = msg.getId();
        Channel channel;
        switch (type) {
            case DATA_CHANNEL_ACK:
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                ChannelKit.setDataChannel(ctx.channel());
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
                break;
            case HEALTH:
                log.debug("get health info");
                break;
            case ACK:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (channel != null && channel.isActive()) {
                    if (msg.getState() == 1)
                    {
                        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                        channel.writeAndFlush(commandResponse);
                    }
                    if (msg.getState() == 0)
                    {
                        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                        channel.writeAndFlush(commandResponse);
                    }
                }
                break;
            case RESPONSE:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (channel != null && channel.isActive())
                {
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(msg.getBytes().length);
                    buf.writeBytes(msg.getBytes());
                    channel.writeAndFlush(buf);
                }
                break;
            case CLOSE:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (channel != null && channel.isActive()) {
                    channel.close();
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
                }
                break;
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            log.error("happen error: ", cause);
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

}
