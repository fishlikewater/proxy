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
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月09日 17:16
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChannelGroupKit {

    /**
     * 目标机路径属性
     */
    public static final AttributeKey<String> CLIENT_PATH = AttributeKey.valueOf("client_path");

    public static final AttributeKey<Channel> DATA_CHANNEL = AttributeKey.valueOf("data_channel");

    /**
     * 请求机绑定目标机属性
     */
    public static final AttributeKey<Channel> CALL_REMOTE_CLIENT = AttributeKey.valueOf("call_remote_client");

    /**
     * 目标主机绑定请求机
     */
    public static final AttributeKey<Channel> CALL_REQUEST_CLIENT = AttributeKey.valueOf("call_request_client");

    /**
     * 客户端类型属性
     */
    public static final AttributeKey<String> CLIENT_TYPE = AttributeKey.valueOf("client_type");

    /**
     * 连接分配的虚拟ip
     */
    public static final AttributeKey<String> VIRT_IP = AttributeKey.valueOf("virt_ip");

    @Getter
    private static final ConcurrentHashMap<String, Channel> CLIENT_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void add(String id, Channel channel) {
        CLIENT_CHANNEL_MAP.put(id, channel);
    }

    public static void remove(String id) {
        CLIENT_CHANNEL_MAP.remove(id);
    }

    public static Channel find(String id) {
        return CLIENT_CHANNEL_MAP.get(id);
    }
}
