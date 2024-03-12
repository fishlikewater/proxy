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

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * udp 中间桥接服务
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年08月26日 10:34
 **/
@Slf4j
@RequiredArgsConstructor
public class ServerUdp {

    private final String address;

    private final int port;

    private EventLoopGroup workGroup;
    private Bootstrap bootstrap;

    void bootstrapConfig() {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            if (EpollKit.epollIsAvailable()) {
                workGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("udp-epoll-work@"));
                bootstrap.group(workGroup).channel(EpollDatagramChannel.class);
            } else {
                workGroup = new NioEventLoopGroup(0, new NamedThreadFactory("udp-nio-work@"));
                bootstrap.group(workGroup).channel(NioDatagramChannel.class);
            }
        }
    }

    public void start() {
        try {
            bootstrapConfig();
            bootstrap.bind(address, port).sync();
        } catch (InterruptedException e) {
            log.error("udp启动失败", e);
            Thread.currentThread().interrupt();
            stop();
        }
    }

    public void stop() {
        try {
            if (this.workGroup != null) {
                this.workGroup.shutdownGracefully().addListener(f -> {
                });
            }
            log.info("⬢ udp shutdown successful");
        } catch (Exception e) {
            log.error("⬢ udp shutdown error");
        }
    }
}
