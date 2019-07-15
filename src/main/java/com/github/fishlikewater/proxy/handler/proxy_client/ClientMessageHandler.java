package com.github.fishlikewater.proxy.handler.proxy_client;


import com.github.fishlikewater.proxy.boot.NettyProxyClient;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @date 2018年12月26日 10:52
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private NettyProxyClient client;

    private Bootstrap clientstrap;

    //保留全局ctx
    private ChannelHandlerContext ctx;

    public ClientMessageHandler(NettyProxyClient client){
        this.client = client;
    }

    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //log.info("连接活动");
        this.ctx = ctx;
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                client.start();
            }
        }, 10, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        MessageProbuf.MessageType type = msg.getType();
        switch (type) {
            case REQUEST:
                String requestid = msg.getRequestId();
                MessageProbuf.Request request = msg.getRequest();
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(request.getHttpVersion()), HttpMethod.valueOf(request.getMethod()), request.getUrl());
                request.getHeaderMap().entrySet().forEach(t->{
                    req.headers().set(t.getKey(), t.getValue());
                });
                req.headers().set("Host", client.getProxyConfig().getLocalAddress() + ":" + client.getProxyConfig().getLocalPort());
                req.content().writeBytes(request.getBody().toByteArray());
                Promise<Channel> promise = createPromise(client.getProxyConfig().getLocalAddress(), client.getProxyConfig().getLocalPort());
                promise.addListener(new FutureListener<Channel>() {
                    @Override
                    public void operationComplete(Future<Channel> channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            ChannelPipeline p = channelFuture.get().pipeline();
                            p.addLast(new ToServerHandler(ctx.channel(), requestid));
                            channelFuture.get().writeAndFlush(req);
                        }
                    }
                });
                break;
            case HEALTH:
                log.info("get receipt health packet from server");
                break;
            case CLOSE:
                ctx.channel().close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("happen error: ", cause);
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        Bootstrap bootstrap = bootstrapConfig();
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            log.warn("connection fail address {}, port {}", host, port);
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }

    private Bootstrap bootstrapConfig(){
        if (clientstrap == null) clientstrap = new Bootstrap();
        else return this.clientstrap;
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            clientstrap.channel(EpollSocketChannel.class);
        } else {
            clientstrap.channel(NioSocketChannel.class);
        }
        clientstrap.group(ctx.channel().eventLoop());
        clientstrap.handler(new TempClientServiceInitializer());
        return clientstrap;
    }
}
