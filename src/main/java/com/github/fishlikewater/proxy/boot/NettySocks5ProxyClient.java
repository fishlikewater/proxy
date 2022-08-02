package com.github.fishlikewater.proxy.boot;


import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.gui.ConnectionUtils;
import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.handler.socks_client.Socks5ClientHandlerInitializer;
import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

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
public class NettySocks5ProxyClient extends NettyProxyClient{

    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap clientstrap;
    private final ConnectionListener connectionListener = new ConnectionListener(this);

    @Getter
    private Channel channel;

    @Getter
    private final ProxyConfig proxyConfig;

    public NettySocks5ProxyClient(ProxyConfig proxyConfig) {
        super(proxyConfig);
        this.proxyConfig = proxyConfig;
    }

    /**
     * 连接配置初始化
     */
    @Override
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
        clientstrap.handler(new Socks5ClientHandlerInitializer(proxyConfig));
    }

    /**
     * 开始连接
     */
    @Override
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
     * 连接成功后的操作
     *
     * @param channel
     */
    @Override
    void afterConnectionSuccessful(Channel channel) {
        final Socks5InitialRequest msg = new DefaultSocks5InitialRequest(Socks5AuthMethod.PASSWORD);
        channel.writeAndFlush(msg);

    }
}
