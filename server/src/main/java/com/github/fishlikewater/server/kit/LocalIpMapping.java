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
package com.github.fishlikewater.server.kit;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 默认ip映射关系实现
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月16日 15:57
 **/
public class LocalIpMapping implements IpMapping {

    private final ConcurrentHashMap<String, Channel> ipMapping = new ConcurrentHashMap<>();

    @Override
    public void put(String ip, Channel channel) {
        channel.closeFuture().addListener(future -> {
            if (future.isSuccess()) {
                remove(ip);
            }
        });
        ipMapping.put(ip, channel);
    }

    @Override
    public Channel getChannel(String ip) {
        return ipMapping.get(ip);
    }

    @Override
    public void remove(String ip) {
        ipMapping.remove(ip);
    }
}
