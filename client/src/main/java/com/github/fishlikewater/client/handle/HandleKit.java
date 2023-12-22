package com.github.fishlikewater.client.handle;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 数据处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月20日 14:40
 **/
@Slf4j
public class HandleKit {

    public static void handlerAck(ChannelHandlerContext ctx, MessageProtocol msg) {
        final Channel socksChannel1 = ctx.channel().attr(Socks5Kit.CHANNELS_SOCKS).get().get(msg.getId());
        if (Objects.nonNull(socksChannel1) && socksChannel1.isActive()) {
            if (msg.getState() == 1) {
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                socksChannel1.writeAndFlush(commandResponse);
            }
            if (msg.getState() == 0) {
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                socksChannel1.writeAndFlush(commandResponse);
            }
        }
    }

    public static void toRegister(MessageProtocol msg, ChannelHandlerContext ctx, ProxyConfig proxyConfig) {
        if (msg.getState() == 1) {
            log.info("验证成功, 开始注册....");
            final MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol
                    .setId(msg.getId())
                    .setCmd(MessageProtocol.CmdEnum.REGISTER)
                    .setState((byte) 1)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
            final String fixedIp = proxyConfig.getFixedIp();
            if (Objects.nonNull(fixedIp)) {
                messageProtocol.setBytes(fixedIp.getBytes(StandardCharsets.UTF_8));
            }
            ctx.writeAndFlush(messageProtocol).addListener(f -> log.info("发送注册信息成功"));
            ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>(16));
        } else {
            log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
        }
    }

    public static void createDataChannel(ChannelHandlerContext ctx, ProxyConfig proxyConfig, String mainChannelId) throws InterruptedException {
        final ChannelFuture future = getChannelFuture(ctx, proxyConfig);
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


    public static void afterRegister(ChannelHandlerContext ctx, ProxyConfig proxyConfig) throws InterruptedException {
        if (StrUtil.isNotBlank(proxyConfig.getLinkIp())) {
            final ChannelFuture future = getChannelFuture(ctx, proxyConfig);
            //连接成功后 发送消息 表明需要建立数据通道
            final MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol
                    .setId(IdUtil.id())
                    .setState((byte) 0)
                    .setCmd(MessageProtocol.CmdEnum.DATA_CHANNEL)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setDst(new MessageProtocol.Dst().setDstAddress(proxyConfig.getLinkIp()))
                    .setBytes(proxyConfig.getToken().getBytes(StandardCharsets.UTF_8));
            future.channel().writeAndFlush(messageProtocol).addListener(future1 -> {
                if (future1.isSuccess()) {
                    Socks5Kit.setChannel(future.channel());
                    Socks5Kit.channel.attr(Socks5Kit.CHANNELS_SOCKS).set(new ConcurrentHashMap<>(16));
                    log.info("成功发送建立数据通道请求...");
                }
            });
        }
    }

    private static ChannelFuture getChannelFuture(ChannelHandlerContext ctx, ProxyConfig proxyConfig) throws InterruptedException {
        final Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline
                        .addLast(new LengthFieldBasedFrameDecoder((int) proxyConfig.getMaxFrameLength().toBytes(), 0, 4))
                        .addLast(new MyByteToMessageCodec())
                        .addLast(new ClientDataHandler());
            }
        });
        bootstrap.remoteAddress(proxyConfig.getAddress(), proxyConfig.getPort());
        return bootstrap.connect().sync();
    }


    public static void handlerConnection(MessageProtocol msg, ChannelHandlerContext ctx) {
        final MessageProtocol.Dst dst = msg.getDst();
        Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new NoneClientInitializer(false));
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


    public static void handlerConnection2(MessageProtocol msg, ChannelHandlerContext ctx, ProxyConfig proxyConfig) {
        final MessageProtocol.Dst dst = msg.getDst();
        if (isAllow(proxyConfig, dst, msg, ctx)) {
            return;
        }
        final ProxyConfig.Mapping mapping = proxyConfig.getMappingMap().get(dst.getDstPort());
        SocketAddress socketAddress = getAddress(mapping, dst.getDstPort());
        Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new NoneClientInitializer(mapping != null && mapping.isSsl()));
        bootstrap.remoteAddress(socketAddress);
        bootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                connectionSuccessAfter(msg, ctx, future);
                final MessageProtocol successMsg = new MessageProtocol();
                successMsg
                        .setCmd(MessageProtocol.CmdEnum.ACK)
                        .setId(msg.getId())
                        .setDst(dst)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 1);
                ctx.channel().writeAndFlush(successMsg);
            } else {
                log.info("连接失败");
                final MessageProtocol failMsg = new MessageProtocol();
                failMsg
                        .setCmd(MessageProtocol.CmdEnum.ACK)
                        .setId(msg.getId())
                        .setDst(dst)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 0);
                ctx.channel().writeAndFlush(failMsg);
            }
        });
    }

    private static SocketAddress getAddress(ProxyConfig.Mapping mapping, int dstPort) {
        if (Objects.nonNull(mapping)) {
            return InetSocketAddress.createUnresolved(mapping.getMappingIp(), mapping.getMappingPort());
        }
        return InetSocketAddress.createUnresolved("localhost", dstPort);
    }


    public static void handlerRequest(MessageProtocol msg, ChannelHandlerContext ctx, ProxyConfig proxyConfig) {
        final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(msg.getId());
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.writeAndFlush(msg.getBytes());
        } else {
            Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
            final MessageProtocol.Dst dst = msg.getDst();
            if (isAllow(proxyConfig, dst, msg, ctx)) {
                return;
            }
            final ProxyConfig.Mapping mapping = proxyConfig.getMappingMap().get(dst.getDstPort());
            SocketAddress socketAddress = getAddress(mapping, dst.getDstPort());
            bootstrap.handler(new NoneClientInitializer(mapping != null && mapping.isSsl()));
            bootstrap.remoteAddress(socketAddress);
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    connectionSuccessAfter(msg, ctx, future);
                    future.channel().writeAndFlush(msg.getBytes());
                } else {
                    log.warn("connect fail...");
                }
            });
        }
    }

    private static boolean isAllow(ProxyConfig proxyConfig, MessageProtocol.Dst dst, MessageProtocol msg, ChannelHandlerContext ctx) {
        if (Objects.nonNull(proxyConfig.getLocalPorts()) && proxyConfig.getLocalPorts().length > 0) {
            boolean find = false;
            for (int localPort : proxyConfig.getLocalPorts()) {
                if (dst.getDstPort() == localPort) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                log.info("不允许连接");
                final MessageProtocol failMsg = new MessageProtocol();
                failMsg
                        .setCmd(MessageProtocol.CmdEnum.ACK)
                        .setId(msg.getId())
                        .setDst(dst)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 0);
                ctx.channel().writeAndFlush(failMsg);
                return true;
            }
        }
        return false;
    }

    private static void connectionSuccessAfter(MessageProtocol msg, ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(msg.getId(), future.channel());
        future.channel().pipeline().addLast(new ByteArrayCodec());
        future.channel().pipeline().addLast(new ChunkedWriteHandler());
        future.channel().pipeline().addLast(new Dest2ClientHandler(ctx, msg.getId(), msg.getDst()));

    }

}
