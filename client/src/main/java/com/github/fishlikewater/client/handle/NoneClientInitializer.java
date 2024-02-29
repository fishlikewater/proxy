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
package com.github.fishlikewater.client.handle;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
@Slf4j
public class NoneClientInitializer extends ChannelInitializer<Channel> {

    private final boolean ssl;

    public NoneClientInitializer(boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    protected void initChannel(Channel ch) throws SSLException {
        if (ssl) {
            try {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                ch.pipeline().addFirst(sslContext.newHandler(ByteBufAllocator.DEFAULT.buffer().alloc()));
            } catch (SSLException e) {
                log.error("初始化ssl异常", e);
                throw e;
            }
        }
    }
}
