package com.github.fishlikewater.callcleint.handle;


import com.github.fishlikewater.callcleint.boot.ProxyClient;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @since: 2018年12月26日 10:52
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private final ProxyClient client;

    public ClientMessageHandler(ProxyClient client) {
        this.client = client;
    }

    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("断开连接");
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) {
        MessageProbuf.MessageType type = msg.getType();
        final String requestId = msg.getRequestId();
        if (type == MessageProbuf.MessageType.VALID){
            log.info(msg.getExtend());
            return;
        }
        if (type == MessageProbuf.MessageType.HEALTH){
            log.debug(msg.getExtend());
            return;
        }
        if (type == MessageProbuf.MessageType.CLOSE) {
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
            if (channel != null && channel.isActive()) {
                channel.close();
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
            }
        } else if (type == MessageProbuf.MessageType.INIT){
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
            if (channel != null && channel.isActive()) {
                if (msg.getExtend().equals("success")){
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    channel.writeAndFlush(commandResponse);
                }
                if (msg.getExtend().equals("fail")){
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    channel.writeAndFlush(commandResponse);
                }
            }
        }else {
            final MessageProbuf.Response response = msg.getResponse();
            final byte[] bytes = response.getBody().toByteArray();
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
            if (channel != null && channel.isActive()) {
                ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
                buf.writeBytes(bytes);
                channel.writeAndFlush(buf);
            }
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
