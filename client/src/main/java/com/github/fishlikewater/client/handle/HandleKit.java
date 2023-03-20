package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.config.BootModel;
import com.github.fishlikewater.kit.IdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月20日 14:40
 **/
@Slf4j
public class HandleKit {


    public static void toRegister(MessageProtocol msg, ChannelHandlerContext ctx, ProxyConfig proxyConfig) {
        if (msg.getState() == 1) {
            log.info("验证成功, 开始注册....");
            final MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol
                    .setId(msg.getId())
                    .setCmd(MessageProtocol.CmdEnum.REGISTER)
                    .setState((byte) 1)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
            if (proxyConfig.getBootModel() == BootModel.ONE_TO_ONE) {
                messageProtocol.setBytes(proxyConfig.getProxyPath().getBytes(StandardCharsets.UTF_8));
            }else {
                messageProtocol.setBytes(proxyConfig.getFixedIp().getBytes(StandardCharsets.UTF_8));
            }
            ctx.writeAndFlush(messageProtocol).addListener(f -> log.info("发送注册信息成功"));
            ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>(16));
        } else {
            log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
        }
    }

    public static void createDataChannel(ChannelHandlerContext ctx, ProxyConfig proxyConfig, String mainChannelId) throws InterruptedException {
        final Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline
                        .addLast(new LengthFieldBasedFrameDecoder(5 * 1024 * 1024, 0, 4))
                        .addLast(new MyByteToMessageCodec())
                        .addLast(new ClientDataHandler());
            }
        });
        bootstrap.remoteAddress(proxyConfig.getAddress(), proxyConfig.getPort());
        ChannelFuture future = bootstrap.connect().sync();
        //连接成功后 发送消息 表明需要建立数据通道
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol
                .setId(IdUtil.id())
                .setState((byte) 1)
                .setCmd(MessageProtocol.CmdEnum.DATA_CHANNEL)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(mainChannelId.getBytes(StandardCharsets.UTF_8));
        future.channel().writeAndFlush(messageProtocol).addListener(future1 -> {
            if (future1.isSuccess()) {
                log.info("成功发送建立数据通道请求...");
            }
        });
    }

    public static void handlerConnection(MessageProtocol msg, ChannelHandlerContext ctx) {
        final MessageProtocol.Dst dst = msg.getDst();
        Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new NoneClientInitializer());
        bootstrap.remoteAddress(dst.getDstAddress(), dst.getDstPort());
        bootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                connectionSuccessAfter(msg, ctx, future);
                final MessageProtocol successMsg = new MessageProtocol();
                successMsg
                        .setCmd(MessageProtocol.CmdEnum.ACK)
                        .setId(msg.getId())
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 1);
                ctx.channel().writeAndFlush(successMsg);
            } else {
                log.debug("连接失败");
                final MessageProtocol failMsg = new MessageProtocol();
                failMsg
                        .setCmd(MessageProtocol.CmdEnum.ACK)
                        .setId(msg.getId())
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 0);
                ctx.channel().writeAndFlush(failMsg);
            }
        });
    }


    public static void handlerRequest(MessageProtocol msg, ChannelHandlerContext ctx, ProxyConfig proxyConfig) {
        final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(msg.getId());
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.writeAndFlush(msg.getBytes());
        } else {
            Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
            final MessageProtocol.Dst dst = msg.getDst();
            bootstrap.handler(new NoneClientInitializer());
            if (Objects.nonNull(proxyConfig)) {
                bootstrap.remoteAddress("127.0.0.1", dst.getDstPort());
            } else {
                bootstrap.remoteAddress(dst.getDstAddress(), dst.getDstPort());
            }
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    connectionSuccessAfter(msg, ctx, future);
                    future.channel().writeAndFlush(msg.getBytes());
                } else {
                    future.channel().writeAndFlush(msg.getBytes());
                }
            });
        }
    }

    private static void connectionSuccessAfter(MessageProtocol msg, ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(msg.getId(), future.channel());
        future.channel().pipeline().addLast(new ByteArrayCodec());
        future.channel().pipeline().addLast(new ChunkedWriteHandler());
        future.channel().pipeline().addLast(new Dest2ClientHandler(ctx, msg.getId(), msg.getDst()));
    }

}
