package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月19日 13:03
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ProxyClient client;

    public ClientMessageHandler(ProxyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelKit.channelGroup.close();
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws InterruptedException {
        final long requestId = msg.getId();
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        switch (cmd) {
            case AUTH:
                //验证成功
                if (msg.getState() == 1)
                {
                    log.info("验证成功, 开始注册....");
                    final MessageProtocol messageProtocol = new MessageProtocol();
                    messageProtocol
                            .setId(requestId)
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setState((byte) 1)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setBytes(client.getProxyConfig().getProxyPath().getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(messageProtocol).addListener(f -> log.info("发送注册信息成功"));
                } else {
                    log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                }
                break;
            case REGISTER:
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case DATA_CHANNEL:
                HandleKit.createDataChannel(ctx, client.getProxyConfig(), new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case HEALTH:
                log.debug("get health info");
                break;
            default:
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
