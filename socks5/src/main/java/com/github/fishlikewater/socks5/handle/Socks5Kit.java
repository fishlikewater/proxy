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
