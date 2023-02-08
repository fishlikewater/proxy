package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ChannelKit;
import com.github.fishlikewater.codec.HttpProtocol;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.IdUtil;
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

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version V1.0
 * @date: 2018年12月25日 14:21
 **/
@Slf4j
@Accessors(chain = true)
public class ProxyClient {

    private final ConnectionListener connectionListener = new ConnectionListener(this);
    @Getter
    private final ProxyConfig proxyConfig;
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap clientstrap;
    @Getter
    private Channel channel;

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
        if (clientstrap == null) {
            clientstrap = new Bootstrap();
            clientstrap.option(ChannelOption.SO_REUSEADDR, true);
            clientstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
            clientstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
            clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            clientstrap.option(ChannelOption.TCP_NODELAY, true);
            clientstrap.option(ChannelOption.SO_KEEPALIVE, true);
            if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
                bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
                clientstrap.group(bossGroup).channel(EpollSocketChannel.class);
            } else {
                bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
                clientstrap.group(bossGroup).channel(NioSocketChannel.class);
            }
            clientstrap.handler(new ClientHandlerInitializer(proxyConfig, proxyConfig.getProxyType(), this));
        }
    }

    /**
     * 开始连接
     */
    public void start() {

        clientstrap.remoteAddress(new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));
        log.info("start {} this port:{} and adress:{}", proxyConfig.getProxyType(), proxyConfig.getPort(), proxyConfig.getAddress());
        try {
            ChannelFuture future = clientstrap.connect().addListener(connectionListener).sync();
            this.channel = future.channel();
            afterConnectionSuccessful(channel);
            ChannelKit.setChannel(this.channel);

        } catch (Exception e) {
            log.error("start {} server fail", proxyConfig.getProxyType());
        }
    }

    /**
     * @description:
     * @param: channel
     * @date: 2022/7/18 14:43
     * @Return: void
     */
    void afterConnectionSuccessful(Channel channel) {

        if (proxyConfig.getProxyType() == ProxyType.proxy_client) {
            final long requestId = IdUtil.id();
            final MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol
                    .setId(requestId)
                    .setCmd(MessageProtocol.CmdEnum.AUTH)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setBytes(proxyConfig.getToken().getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(messageProtocol).addListener(f -> log.info("发送验证信息成功"));
        } else {
            final HttpProtocol httpProtocol = new HttpProtocol();
            httpProtocol.setId(IdUtil.id());
            httpProtocol.setCmd(HttpProtocol.CmdEnum.AUTH);
            httpProtocol.setBytes(proxyConfig.getToken().getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(httpProtocol).addListener(f -> log.info("发送验证信息成功"));
            channel.attr(ChannelKit.CHANNELS_LOCAL).set(new ConcurrentHashMap<>());
        }

    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyConfig.getProxyType());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().addListener(f -> {

                });
            }
            log.info("⬢ {} shutdown successful", proxyConfig.getProxyType());
        } catch (Exception e) {
            log.error("⬢ {} shutdown error", proxyConfig.getProxyType());
        }
    }

}
