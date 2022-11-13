package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ChannelKit;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.kit.NamedThreadFactory;
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
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyClient
 * @Description
 * @date 2018年12月25日 14:21
 **/
@Slf4j
@Accessors(chain = true)
public class ProxyClient{

    private final ConnectionListener connectionListener = new ConnectionListener(this);
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap clientstrap;
    @Getter
    private Channel channel;
    @Getter
    private final ProxyConfig proxyConfig;

    public ProxyClient(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;

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
        clientstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
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
        clientstrap.handler(new ClientHandlerInitializer(proxyConfig, ProxyType.proxy_client, this));
    }

    /**
     * 开始连接
     */
    public void start() {

        clientstrap.remoteAddress(new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));
        log.info("start {} this port:{} and adress:{}", ProxyType.proxy_client, proxyConfig.getPort(), proxyConfig.getAddress());
        try {
            ChannelFuture future = clientstrap.connect().addListener(connectionListener).sync();
            this.channel = future.channel();
            afterConnectionSuccessful(channel);
            ChannelKit.setChannel(this.channel);

        } catch (Exception e) {
            log.error("start {} server fail", ProxyType.proxy_client);
        }
    }

    /**
     * @Description :
     * @param: channel
     * @Date : 2022/7/18 14:43
     * @Author : fishlikewater@126.com
     * @Return : void
     */
    void afterConnectionSuccessful(Channel channel) {
        /* 发送首先发送验证信息*/
        MessageProbuf.Register.Builder builder = MessageProbuf.Register.newBuilder();
        if (proxyConfig.getProxyType() == ProxyType.http){
            final Set<String> keySet = ChannelKit.HTTP_MAPPING_MAP.keySet();
            final String path = String.join(",", keySet);
            builder.setPath(path);
        }else {
            builder.setPath(proxyConfig.getProxyPath());
        }
        builder.setToken(proxyConfig.getToken());
        final MessageProbuf.Message.Builder messageBuild = MessageProbuf.Message
                .newBuilder()
                .setRequestId(IdUtil.next())
                .setRegister(builder.build())
                .setExtend("client")
                .setType(MessageProbuf.MessageType.VALID);
        channel.writeAndFlush(messageBuild.build()).addListener(f -> log.info("发送验证信息成功"));
        channel.attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", ProxyType.proxy_client);
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().addListener(f -> {

                });
            }
            log.info("⬢ {} shutdown successful", ProxyType.proxy_client);
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", ProxyType.proxy_client);
        }
    }

}
