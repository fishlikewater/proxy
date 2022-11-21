package com.github.fishlikewater.server.kit;

import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since: 2019年07月09日 17:16
 **/
@Slf4j
public class ChannelGroupKit {

    //http服务器 标记本次请求
    public final static AttributeKey<Long> CHANNELS_LOCAL = AttributeKey.newInstance("CHANNELS_LOCAL");

    //目标机路径属性
    public static final AttributeKey<String> CLIENT_PATH = AttributeKey.valueOf("client_path");

    //请求机绑定目标机属性
    public static final AttributeKey<Channel> CALL_REMOTE_CLIENT = AttributeKey.valueOf("call_remote_client");

    //目标主机绑定请求机
    public static final AttributeKey<Channel> CALL_REQUEST_CLIENT = AttributeKey.valueOf("call_request_client");

    //客户端类型属性
    public static final AttributeKey<String> CLIENT_TYPE = AttributeKey.valueOf("client_type");

    @Getter
    private static final ConcurrentHashMap<String, Channel> clientChannelMap = new ConcurrentHashMap<>();

    public static void add(String path, Channel channel){
        clientChannelMap.put(path, channel);
    }
    public static void remove(String path){
        clientChannelMap.remove(path);
    }
    public static Channel find(String path){
        return clientChannelMap.get(path);
    }





    private static final MessageProbuf.Message respSuccessVailMsg = MessageProbuf.Message.newBuilder()
                                .setType(MessageProbuf.MessageType.VALID).setExtend("SUCCESS").build();

    public static void sendVailSuccess(Channel channel){
        channel.writeAndFlush(respSuccessVailMsg);
    }

    public static void sendVailFail(Channel channel, String failCause){
        MessageProbuf.Message respFailVailMsg = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.VALID).setExtend(failCause).build();
        channel.writeAndFlush(respFailVailMsg).addListener(f-> channel.close());
    }

}
