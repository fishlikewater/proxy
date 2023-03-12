package com.github.fishlikewater.callclient.boot;


import com.github.fishlikewater.callclient.config.ProxyConfig;
import com.github.fishlikewater.callclient.handle.ChannelKit;
import com.github.fishlikewater.callclient.handle.ClientHandlerInitializer;
import com.github.fishlikewater.codec.MessageProtocol;
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

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2018年12月25日 14:21
 **/
@Slf4j
@Accessors(chain = true)
public class ProxyClient{

    private final ConnectionListener connectionListener = new ConnectionListener(this);
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
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
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
        }
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        if (EpollKit.epollIsAvailable()) {
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
            bootstrap.group(bossGroup).channel(EpollSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
            bootstrap.group(bossGroup).channel(NioSocketChannel.class);
        }
        bootstrap.handler(new ClientHandlerInitializer(proxyConfig, this));
    }

    /**
     * 开始连接
     */
    public void start() {
        bootstrap.remoteAddress(new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));
        log.info("start call-client this port:{} and address:{}", proxyConfig.getPort(), proxyConfig.getAddress());
        try {
            ChannelFuture future = bootstrap.connect().addListener(connectionListener).sync();
            this.channel = future.channel();

            afterConnectionSuccessful(channel);
            ChannelKit.setChannel(this.channel);

        } catch (Exception e) {
            log.error("start call-client server fail");
        }
    }

    /**
     * 注册
     */
    void afterConnectionSuccessful(Channel channel) {
        /* 发送首先发送验证信息*/
        final long requestId = IdUtil.id();
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol
                .setId(requestId)
                .setCmd(MessageProtocol.CmdEnum.AUTH)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(proxyConfig.getToken().getBytes(StandardCharsets.UTF_8));
        channel.writeAndFlush(messageProtocol).addListener(f -> log.info("发送验证信息成功"));
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {call-client shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().addListener(f -> {

                });
            }
            log.info("⬢ call-client shutdown successful");
        } catch (Exception e) {
            log.error("⬢ call-client shutdown error", e);
        }
    }
}
