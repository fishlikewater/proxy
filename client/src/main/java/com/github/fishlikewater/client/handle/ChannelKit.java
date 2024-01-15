package com.github.fishlikewater.client.handle;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年08月24日 22:04
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChannelKit {

    public static final AttributeKey<Map<Long, Channel>> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");

    private static Channel channel = null;

    public static void setChannel(Channel channel) {
        ChannelKit.channel = channel;
    }

    public static Channel getChannel() {
        return ChannelKit.channel;
    }

}
