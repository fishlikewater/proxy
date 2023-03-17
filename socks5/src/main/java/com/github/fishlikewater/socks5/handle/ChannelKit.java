package com.github.fishlikewater.socks5.handle;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author fishlikewater@126.com
 * @since 2019年08月24日 22:04
 */
public class ChannelKit {

    public final static AttributeKey<Long> LOCAL_INFO = AttributeKey.newInstance("LOCAL_INFO");
    public final static AttributeKey<Map<Long, Channel>> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");

    @Setter
    @Getter
    private static Channel channel = null;

    @Setter
    @Getter
    private static Channel dataChannel = null;

}
