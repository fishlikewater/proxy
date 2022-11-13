package com.github.fishlikewater.client.handle;


import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
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

    //保留全局ctx
    private final ProxyConfig proxyConfig;
    private final ProxyClient client;

    public ClientMessageHandler(ProxyConfig proxyConfig, ProxyClient client) {
        this.proxyConfig = proxyConfig;
        this.client = client;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //log.info("连接活动");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ctx.close();
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) {
        MessageProbuf.MessageType type = msg.getType();
        final MessageProbuf.Protocol protocol = msg.getProtocol();
        if (protocol == MessageProbuf.Protocol.HTTP) {
            HandleKit.handleHttp(ctx, msg);
        }
        if (protocol == MessageProbuf.Protocol.SOCKS){
            HandleKit.handleSocks(ctx, msg, type);
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
