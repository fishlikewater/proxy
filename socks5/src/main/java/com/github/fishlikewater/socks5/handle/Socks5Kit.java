package com.github.fishlikewater.socks5.handle;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Setter;

import java.util.Map;

/**
 * @author fishlikewater@126.com
 * @since 2019年08月24日 22:04
 */
public class Socks5Kit {

    public final static AttributeKey<Long> LOCAL_INFO = AttributeKey.newInstance("LOCAL_INFO");
    public final static AttributeKey<Map<Long, Channel>> CHANNELS_SOCKS = AttributeKey.newInstance("CHANNELS_SOCKS");

    @Setter
    public static Channel channel;

}
