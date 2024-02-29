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
package com.github.fishlikewater.socks5.handle;

import com.github.fishlikewater.socks5.config.Socks5Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fishlikewater@126.com
 * @since 2019年02月26日 21:47
 **/
@Slf4j
public class Socks5Initializer extends ChannelInitializer<Channel> {

    private final Socks5Config socks5Config;

    public Socks5Initializer(Socks5Config socks5Config) {
        log.info("init handler");
        this.socks5Config = socks5Config;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addFirst(new Socks5CommandRequestDecoder());
        if (socks5Config.isAuth()) {
            /* 添加验证机制*/
            p.addFirst(new Socks5PasswordAuthRequestHandler(socks5Config));
            p.addFirst(new Socks5PasswordAuthRequestDecoder());
        }
        p.addFirst(new Socks5InitialAuthHandler(socks5Config.isAuth()));
        p.addFirst(Socks5ServerEncoder.DEFAULT);
        p.addFirst(new Socks5InitialRequestDecoder());
        /* Socks connection handler */
        p.addLast(new Socks5CommandRequestHandler(socks5Config));
    }
}
