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
package com.github.fishlikewater.codec;

import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * @author fishlikewater@126.com
 * @since 2023/3/10 10:25
 */

public class ByteArrayCodec extends CombinedChannelDuplexHandler<ByteArrayDecoder, ByteArrayEncoder> {

    public ByteArrayCodec() {
        super(new ByteArrayDecoder(), new ByteArrayEncoder());
    }
}
