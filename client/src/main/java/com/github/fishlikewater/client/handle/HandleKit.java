package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactroy;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.ByteArrayCodec;
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

    public static void handleHttp(ChannelHandlerContext ctx, MessageProbuf.Message msg) {
        switch (msg.getType()) {
            case REQUEST:
                String requestid = msg.getRequestId();
                MessageProbuf.Request request = msg.getRequest();
                String name = msg.getExtend();
                final ProxyConfig.HttpMapping httpMapping = ChannelKit.HTTP_MAPPING_MAP.get(name);
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(request.getHttpVersion()), HttpMethod.valueOf(request.getMethod()), request.getUrl());
                request.getHeaderMap().forEach((key, value) -> req.headers().set(key, value));
                req.headers().set("Host", (httpMapping.getAddress() + ":" + httpMapping.getPort()));
                req.content().writeBytes(request.getBody().toByteArray());
                Promise<Channel> promise = createPromise(httpMapping.getAddress(), httpMapping.getPort(), ctx);
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
