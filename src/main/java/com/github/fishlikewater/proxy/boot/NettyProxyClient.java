package com.github.fishlikewater.proxy.boot;


import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.gui.ConnectionUtils;
import com.github.fishlikewater.proxy.handler.health.ClientHeartBeatHandler;
import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.handler.ClientHandlerInitializer;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.IdUtil;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
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

    private final ConnectionListener connectionListener = new ConnectionListener(this);
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
     */
    void bootstrapConfig() {
        if (clientstrap == null) clientstrap = new Bootstrap();
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000);
        clientstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        clientstrap.option(ChannelOption.TCP_NODELAY, true);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
            clientstrap.group(bossGroup).channel(EpollSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
            clientstrap.group(bossGroup).channel(NioSocketChannel.class);
        }
        clientstrap.handler(new ClientHandlerInitializer(proxyConfig, this));
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
            ConnectionUtils.setStateText(StrUtil.format("start {} this port:{} and adress:{}", proxyConfig.getType(), proxyConfig.getPort(), proxyConfig.getAddress()));
            ConnectionUtils.setConnState(true);
            afterConnectionSuccessful(channel);
            ChannelKit.setChannel(this.channel);
            //future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("start {} server fail", proxyConfig.getType());
            ConnectionUtils.setStateText("连接失败");
            ConnectionUtils.setConnState(false);
        }
    }

    /**
     * @Description :
     * @param : channel
     * @Date : 2022/7/18 14:43
     * @Author : fishlikewater@126.com
     * @Return : void
     */
    void afterConnectionSuccessful(Channel channel) throws InterruptedException {
        /* 先发一个测试包*/
        channel.writeAndFlush(ClientHeartBeatHandler.HEARTBEAT_SEQUENCE);
        Thread.sleep(5000);
        /* 发送首先发送验证信息*/
        MessageProbuf.Register.Builder builder = MessageProbuf.Register.newBuilder();
        builder.setPath(proxyConfig.getProxyPath()).setToken(proxyConfig.getToken());
        MessageProbuf.Message validMessage = MessageProbuf.Message
                .newBuilder()
                .setRequestId(IdUtil.next())
                .setRegister(builder.build())
                .setType(MessageProbuf.MessageType.VALID)
                .build();
        channel.writeAndFlush(validMessage).addListener(f -> {
            log.info("发送验证信息成功");
        });
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyConfig.getType());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().addListener(f->{
                    if(f.isSuccess()){
                        ConnectionUtils.reset();
                    }
                });
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
