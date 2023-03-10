package com.github.fishlikewater.codec;

import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
/**
 *
 * @author fishlikewater@126.com
 * @since 2023/3/10 10:25
 */

public class ByteArrayCodec extends CombinedChannelDuplexHandler<ByteArrayDecoder, ByteArrayEncoder> {

    public ByteArrayCodec() {
        super(new ByteArrayDecoder(), new ByteArrayEncoder());
    }
}
