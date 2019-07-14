package com.github.fishlikewater.proxy.boot;


import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.proxy_client.ClientHandlerInitializer;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

/**
 * @version V1.0
 * @a1uthor zhangx
 * @mail fishlikewater@126.com
 * @ClassName NettyProxyClient
 * @Description
 * @date 2018年12月25日 14:21
 **/
@Slf4j
@Accessors(chain = true)
public class NettyProxyClient {

    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap clientstrap;

    @Getter
    private Channel channel;

    private ConnectionListener connectionListener = new ConnectionListener(this);
    @Getter
    private ProxyConfig proxyConfig;

    public NettyProxyClient(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    private NettyProxyClient() {

    }

    /**
     * 首次初始化连接
     */
    public void run() {
        bootstrapConfig();
        start();
    }

    /**
     * 连接配置初始化
     * @return
     */
    private Bootstrap bootstrapConfig() {
        if (clientstrap == null) clientstrap = new Bootstrap();
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
            clientstrap.group(bossGroup).channel(EpollSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
            clientstrap.group(bossGroup).channel(NioSocketChannel.class);
        }
        clientstrap.handler(new ClientHandlerInitializer(proxyConfig, this));
        return clientstrap;
    }

    /**
     * 开始连接
     */
    public void start() {
        clientstrap.remoteAddress(new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));
        try {
            ChannelFuture future = clientstrap.connect().addListener(connectionListener).sync();
            this.channel = future.channel();
            log.info("start {} this port:{} and adress:{}", proxyConfig.getType(), proxyConfig.getPort(), proxyConfig.getAddress());
            afterConnectionSuccessful(channel);
            //future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("start {} server fail", proxyConfig.getType());
        }
    }

    /**
     * 连接成功后的操作
     *
     * @param channel
     */
    private void afterConnectionSuccessful(Channel channel) {
        /** 发送首先发送验证信息*/
        MessageProbuf.Message validMessage = MessageProbuf.Message
                .newBuilder()
                .setBody("token")
                .setExtend(proxyConfig.getProxyPath())
                .setType(MessageProbuf.MessageType.VALID)
                .build();
        channel.writeAndFlush(validMessage).addListener(f -> {
            if (f.isSuccess()) {
                channel.writeAndFlush(MessageProbuf.Message
                        .newBuilder()
                        .setBody("init")
                        .setType(MessageProbuf.MessageType.CONNECTION)
                        .build());
            }
        });
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyConfig.getType());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully();
            }
            log.info("⬢ {} shutdown successful", proxyConfig.getType());
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", proxyConfig.getType());
        }
    }

    private void registerShutdownHook(Supplier supplier) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("startting shutdown working......");
                    supplier.get();
                } catch (Throwable e) {
                    log.error("shutdownHook error", e);
                } finally {
                    log.info("jvm shutdown");
                }
            }

        });
    }

}