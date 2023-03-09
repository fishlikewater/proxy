package com.github.fishlikewater.callcleint.handle;


import com.github.fishlikewater.callcleint.boot.ProxyClient;
import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @since 2018年12月26日 10:52
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

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
        ChannelKit.getDataChannel().close();
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
        MessageProtocol.CmdEnum type = msg.getCmd();
        final long requestId = msg.getId();
        switch (type) {
            case AUTH:
                if (msg.getState() == 1)//验证成功
                {
                    log.info("验证成功, 开始注册....");
                    final MessageProtocol messageProtocol = new MessageProtocol();
                    messageProtocol
                            .setId(requestId)
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setBytes(client.getProxyConfig().getProxyPath().getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(messageProtocol).addListener(f -> log.info("发送注册信息成功"));
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
                }
                break;
            case REGISTER:
                final byte[] bytes = msg.getBytes();
                final int state = msg.getState();
                final String registerInfo = new String(bytes, StandardCharsets.UTF_8);
                if (state == 1) {
                    log.info("开始建立数据传输通道...");
                    //注册成功, 去建立数据传输通道
                    HandleKit.createDataChannel(ctx, client.getProxyConfig(), registerInfo);
                } else {
                    log.info(registerInfo);
                }
                break;
            case HEALTH:
                log.debug("get health info");
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
