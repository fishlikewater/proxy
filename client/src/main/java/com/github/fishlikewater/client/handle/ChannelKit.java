package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年08月24日 22:04
 **/
public class ChannelKit {

    public final static AttributeKey<Long> LOCAL_INFO = AttributeKey.newInstance("LOCAL_INFO");
    public final static AttributeKey<Map<Long, Channel>> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");

    @Setter
    @Getter
    public static Map<String, ProxyConfig.HttpMapping> HTTP_MAPPING_MAP = new ConcurrentHashMap<>();

    @Setter
    @Getter
    public static Channel channel = null;


    public static void sendMessage(MessageProbuf.Message message, GenericFutureListener<? extends Future<? super Void>> listener){
        if(channel != null && channel.isActive()){
            channel.writeAndFlush(message).addListener(listener);
        }
    }
}
