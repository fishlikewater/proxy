package com.github.fishlikewater.client.handle;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年08月24日 22:04
 **/
public class ChannelKit {

    public final static ChannelGroup channelGroup =  new DefaultChannelGroup((GlobalEventExecutor.INSTANCE));

    public final static AttributeKey<Map<Long, Channel>> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");


    @Setter
    @Getter
    public static Channel channel = null;

}
