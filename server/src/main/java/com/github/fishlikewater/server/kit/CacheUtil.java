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

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月13日 12:58
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtil {

    @Getter
    private static Cache<Long, Channel> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
            .limit(100)
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .buildCache();

    public static void init(Cache<Long, Channel> cache) {
        CacheUtil.cache = cache;
    }

    public static void put(Long requestId, Channel channel, long ex) {
        cache.put(requestId, channel, ex, TimeUnit.SECONDS);
    }

    public static void remove(Long requestId) {
        cache.remove(requestId);
    }

    public static Channel get(Long requestId) {
        return cache.get(requestId);
    }
}
