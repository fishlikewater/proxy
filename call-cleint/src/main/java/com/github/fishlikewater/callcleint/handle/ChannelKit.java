package com.github.fishlikewater.callcleint.handle;

import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年08月24日 22:04
 **/
public class ChannelKit {

    public final static AttributeKey<Channel> CHANNELS = AttributeKey.newInstance("CHANNELS");
    public final static AttributeKey<String> LOCAL_INFO = AttributeKey.newInstance("LOCAL_INFO");
    public final static AttributeKey<Map<String, Channel>> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");

    @Setter
    @Getter
    public static Channel channel = null;


    public static void sendMessage(MessageProbuf.Message message, GenericFutureListener<? extends Future<? super Void>> listener){
        if(channel != null && channel.isActive()){
            channel.writeAndFlush(message).addListener(listener);
        }
    }
}
