package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactroy;
import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月19日 13:03
 **/
@Slf4j
public class ProxyTcpMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ProxyClient client;

    public ProxyTcpMessageHandler(ProxyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ctx.close();
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final Long requestId = msg.getId();
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        Channel channel;
        switch (cmd){
            case AUTH:
                if (msg.getState() == 1)//验证成功
                {
                    log.info("验证成功, 开始注册....");
                    final MessageProtocol messageProtocol = new MessageProtocol();
                    messageProtocol
                            .setId(requestId)
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setState((byte)1)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setBytes(client.getProxyConfig().getProxyPath().getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(messageProtocol).addListener(f -> log.info("发送注册信息成功"));
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
                }else
                {
                    log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                }
                break;
            case REGISTER:
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case HEALTH:
                log.debug("get health info");
                break;
            case CLOSE:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)){
                    channel.close();
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
                }
                break;
            case REQUEST:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)){
                    channel.writeAndFlush(msg.getBytes());
                }
                break;
            case CONNECTION:
                final MessageProtocol.Dst dst = msg.getDst();
                Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
                bootstrap.handler(new NoneClientInitializer());
                bootstrap.remoteAddress(dst.getDstAddress(), dst.getDstPort());
                bootstrap.connect().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(requestId, future.channel());
                        future.channel().pipeline().addLast(new ByteArrayCodec());
                        future.channel().pipeline().addLast(new ChunkedWriteHandler());
                        future.channel().pipeline().addLast(new Dest2ClientHandler(ctx, requestId));
                        log.debug("连接成功");
                        final MessageProtocol successMsg = new MessageProtocol();
                        successMsg
                                .setCmd(MessageProtocol.CmdEnum.ACK)
                                .setId(requestId)
                                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                                .setState((byte)1);
                        ctx.channel().writeAndFlush(successMsg);
                    } else {
                        log.debug("连接失败");
                        final MessageProtocol failMsg = new MessageProtocol();
                        failMsg
                                .setCmd(MessageProtocol.CmdEnum.ACK)
                                .setId(requestId)
                                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                                .setState((byte)0);
                        ctx.channel().writeAndFlush(failMsg);
                    }
                });

        }
    }
}
