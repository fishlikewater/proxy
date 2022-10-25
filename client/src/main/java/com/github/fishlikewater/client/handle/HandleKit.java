package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactroy;
import com.github.fishlikewater.client.boot.ClientHandlerInitializer;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月25日 16:10
 **/
@Slf4j
public class HandleKit {

    public static void handleTcp(ChannelHandlerContext ctx, MessageProbuf.Message msg,
                                 MessageProbuf.MessageType type, ProxyConfig proxyConfig) {
        final Map<String, String> headerMap = msg.getRequest().getHeaderMap();
        final String flag = headerMap.get("address") + ":" +  headerMap.get("port");
        if (type == MessageProbuf.MessageType.CLOSE) {
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(flag);
            if (channel != null && channel.isActive()) {
                channel.close();
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(flag);
            }
        } else {
            final byte[] bytes = msg.getRequest().getBody().toByteArray();
            if (type == MessageProbuf.MessageType.REQUEST) {
                //先判断是否建立连接
                final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(flag);
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(bytes);
                } else {
                    Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
                    bootstrap.handler(new ClientHandlerInitializer(proxyConfig, ProxyType.tcp_client, null));
                    bootstrap.remoteAddress(headerMap.get("address"), Integer.parseInt(headerMap.get("port")));
                    bootstrap.connect().addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(flag, future.channel());
                            future.channel().attr(ChannelKit.LOCAL_INFO).set(msg.getRequestId());
                            future.channel().writeAndFlush(bytes);
                            log.info("连接成功");
                        } else {
                            log.warn("连接失败");
                        }
                    });

                }
            }
        }
    }

    public static void handleHttp(ChannelHandlerContext ctx, MessageProbuf.Message msg, ProxyConfig proxyConfig) {
        switch (msg.getType()) {
            case REQUEST:
                String requestid = msg.getRequestId();
                MessageProbuf.Request request = msg.getRequest();
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(request.getHttpVersion()), HttpMethod.valueOf(request.getMethod()), request.getUrl());
                request.getHeaderMap().forEach((key, value) -> req.headers().set(key, value));
                req.headers().set("Host", (proxyConfig.getHttpAdress() + ":" + proxyConfig.getHttpPort()));
                req.content().writeBytes(request.getBody().toByteArray());
                Promise<Channel> promise = createPromise(proxyConfig.getHttpAdress(), proxyConfig.getHttpPort(), ctx);
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ToServerHandler(requestid));
                        channelFuture.get().writeAndFlush(req);
                    }
                });
                break;
            case VALID:
                String extend = msg.getExtend();
                if (!"SUCCESS".equals(extend)) {
                    log.warn(extend);
                } else {
                    log.info("验证成功");
                }
                break;
            case HEALTH:
                log.info("get receipt health packet from server");
                break;
            case CLOSE:
                ctx.channel().close();
        }
    }

    public static void handleSocks(ChannelHandlerContext ctx, MessageProbuf.Message msg, MessageProbuf.MessageType type){
        final String requestId = msg.getRequestId();
        final String callId = msg.getExtend();
        if (type == MessageProbuf.MessageType.INIT){
            final MessageProbuf.Socks scoks = msg.getScoks();
            Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
            bootstrap.handler(new NoneClientInitializer());
            bootstrap.remoteAddress(scoks.getAddress(), scoks.getPort());
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().put(requestId, future.channel());
                    future.channel().pipeline().addLast(new ByteArrayCodec());
                    future.channel().pipeline().addLast(new ChunkedWriteHandler());
                    future.channel().pipeline().addLast(new Dest2ClientHandler(ctx, requestId, callId));
                    log.debug("连接成功");
                    final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                            .setRequestId(requestId)
                            .setProtocol(MessageProbuf.Protocol.SOCKS)
                            .setType(MessageProbuf.MessageType.INIT)
                            .setExtend("success")
                            .setClientId(callId)
                            .build();
                    ctx.channel().writeAndFlush(message);
                } else {
                    log.debug("连接失败");
                    final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                            .setRequestId(requestId)
                            .setProtocol(MessageProbuf.Protocol.SOCKS)
                            .setType(MessageProbuf.MessageType.INIT)
                            .setExtend("fail")
                            .setClientId(callId)
                            .build();
                    ctx.channel().writeAndFlush(message);
                }
            });
        }
        if (type == MessageProbuf.MessageType.REQUEST){
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
            final byte[] bytes = msg.getRequest().getBody().toByteArray();
            channel.writeAndFlush(bytes);
        }
        if (type == MessageProbuf.MessageType.CLOSE){
            final Channel channel = ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().get(requestId);
            if (Objects.nonNull(channel)){
                channel.close();
                ctx.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
            }

        }
    }





    //根据host和端口，创建一个连接web的连接
    private static Promise<Channel> createPromise(String host, int port, ChannelHandlerContext ctx) {
        Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
        bootstrap.handler(new TempClientServiceInitializer());
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        promise.setSuccess(channelFuture.channel());
                    } else {
                        log.debug("connection fail address {}, port {}", host, port);
                        channelFuture.cancel(true);
                    }
                });
        return promise;
    }

}
