package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月22日 18:59
 **/
@Slf4j
public class ClientDataHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        Channel channel;
        final long requestId = msg.getId();
        switch (msg.getCmd()) {
            case HEALTH:
                log.debug("get health info");
                break;
            case DATA_CHANNEL_ACK:
                ChannelKit.channelGroup.add(ctx.channel());
                log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
                break;
            case CLOSE:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)) {
                    channel.close();
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
                }
                break;
            case REQUEST:
                channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
                if (Objects.nonNull(channel)) {
                    channel.writeAndFlush(msg.getBytes());
                }
                break;
            case CONNECTION:
                final MessageProtocol.Dst dst = msg.getDst();
                Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
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
                break;
            default:
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("数据传输通道断开");
        final Map<Long, Channel> longChannelMap = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get();
        longChannelMap.forEach((k, v) -> v.close());
        longChannelMap.clear();
        super.channelInactive(ctx);
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
