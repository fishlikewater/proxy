/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ChannelKit;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.socks5.boot.SocksServerBoot;
import com.github.fishlikewater.socks5.config.Socks5Config;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
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
import java.util.Objects;

/**
 * @author fishl
 * @version V1.0
 * @since 2018年12月25日 14:21
 **/
@Slf4j
@Accessors(chain = true)
public class ProxyClient {

    private final ConnectionListener connectionListener = new ConnectionListener(this);
    @Getter
    private final ProxyConfig proxyConfig;
    private final Socks5Config socks5Config;
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
    private SocksServerBoot socksServerBoot;
    @Getter
    private Channel channel;

    public ProxyClient(ProxyConfig proxyConfig, Socks5Config socks5Config) {
        this.proxyConfig = proxyConfig;
        this.socks5Config = socks5Config;
    }

    public void run() {
        bootstrapConfig();
        start();
    }

    void bootstrapConfig() {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            if (EpollKit.epollIsAvailable()) {
                bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
                bootstrap.group(bossGroup).channel(EpollSocketChannel.class);
            } else {
                bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
                bootstrap.group(bossGroup).channel(NioSocketChannel.class);
            }
            bootstrap.handler(new ClientHandlerInitializer(proxyConfig, this));
        }
    }

    public void start() {

        bootstrap.remoteAddress(new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));
        log.info("start client this port:{} and address:{}", proxyConfig.getPort(), proxyConfig.getAddress());
        try {
            ChannelFuture future = bootstrap.connect().addListener(connectionListener).sync();
            this.channel = future.channel();
            afterConnectionSuccessful(channel);
            ChannelKit.setChannel(this.channel);
            Socks5Kit.setChannel(this.channel);
            if (proxyConfig.isOpenSocks5()) {
                if (Objects.nonNull(socksServerBoot)) {
                    socksServerBoot.stop();
                } else {
                    socksServerBoot = new SocksServerBoot(socks5Config);
                }
                socksServerBoot.start();
            }
        } catch (InterruptedException e) {
            log.error("start client server fail", e);
            Thread.currentThread().interrupt();
        }
    }

    void afterConnectionSuccessful(Channel channel) {
        final long requestId = IdUtil.id();
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol
                .setId(requestId)
                .setCmd(MessageProtocol.CmdEnum.AUTH)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(proxyConfig.getToken().getBytes(StandardCharsets.UTF_8));
        channel.writeAndFlush(messageProtocol).addListener(f -> log.info("发送验证信息成功"));
    }

    public void stop() {
        if (Objects.nonNull(socksServerBoot)) {
            socksServerBoot.stop();
        }
        log.info("⬢ client shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().addListener(f -> {
                });
            }
            log.info("⬢ client shutdown successful");
        } catch (Exception e) {
            log.error("⬢ client shutdown error");
        }
    }
}
