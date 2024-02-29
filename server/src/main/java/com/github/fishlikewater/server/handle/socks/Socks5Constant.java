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
package com.github.fishlikewater.server.handle.socks;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fishlikewater@126.com
 * @since 2022年10月23日 15:35
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Socks5Constant {

    public static final AttributeKey<String> ACCOUNT = AttributeKey.valueOf("account");

    @Setter
    protected static Map<String, AtomicLong> accountFlow = new ConcurrentHashMap<>();

    @Setter
    @Getter
    protected static Map<String, String> accountMap = new ConcurrentHashMap<>();
}
