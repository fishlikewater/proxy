package com.github.fishlikewater.proxy.handler.proxy_client;

import com.github.fishlikewater.proxy.kit.MessageProbuf;
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

    @Setter
    @Getter
    public static Channel channel = null;

    @Setter
    @Getter
    public static Channel localChannel = null;


    public static void sendMessage(MessageProbuf.Message message, GenericFutureListener<? extends Future<? super Void>> listener){
        if(channel != null && channel.isActive()){
            channel.writeAndFlush(message).addListener(listener);
        }
    }

    public static void sendLocalMessage(MessageProbuf.Message message, GenericFutureListener<? extends Future<? super Void>> listener){
        if(localChannel != null && localChannel.isActive()){
            localChannel.writeAndFlush(message).addListener(listener);
        }
    }
}
