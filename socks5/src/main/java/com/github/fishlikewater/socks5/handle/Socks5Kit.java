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

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author fishlikewater@126.com
 * @since 2019年08月24日 22:04
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Socks5Kit {

    public static final AttributeKey<Long> LOCAL_INFO = io.netty.util.AttributeKey.newInstance("LOCAL_INFO");
    public static final AttributeKey<Map<Long, Channel>> CHANNELS_SOCKS = io.netty.util.AttributeKey.newInstance("CHANNELS_SOCKS");
    private static Channel channel;

    public static void setChannel(Channel channel) {
        Socks5Kit.channel = channel;
    }

    public static Channel getChannel() {
        return Socks5Kit.channel;
    }
}
