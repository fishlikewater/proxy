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
package com.github.fishlikewater.server.boot;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.server.config.ProxyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年02月26日 21:45
 **/
@Slf4j
public class ServerTcp {
    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    private EventLoopGroup workerGroup;

    private final ProxyConfig proxyConfig;

    private final ProxyType proxyType;

    public ServerTcp(ProxyConfig proxyConfig, ProxyType proxyType) {
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
    }

    public void start() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        if (EpollKit.epollIsAvailable()) {
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-boss@"));
            workerGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-worker@"));
            bootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@"));
            workerGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-worker@"));
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        }
        bootstrap.childHandler(new ServiceInitializer(proxyConfig, proxyType));
        try {
            Channel ch = bootstrap.bind(proxyConfig.getAddress(), proxyConfig.getPort()).sync().channel();
            log.info("⬢ start server this port:{} and address:{} proxy type:{}", proxyConfig.getPort(), proxyConfig.getAddress(), proxyType);
            ch.closeFuture().addListener(t -> log.info("⬢  {}服务开始关闭", proxyType));
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ {} shutdown ...", proxyType);
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
            log.info("⬢ {} shutdown successful", proxyType);
        } catch (InterruptedException e) {
            log.error("⬢ {} shutdown error", proxyType, e);
            Thread.currentThread().interrupt();
        }
    }
}
