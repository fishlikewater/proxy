package com.github.fishlikewater.proxy.kit;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月09日 17:16
 * @since
 **/
@Slf4j
public class ChannelGroupKit {

    private static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

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

    public static void add(Channel channel){
        group.add(channel);
    }

    public static void remove(Channel channel){
        final String string = channel.localAddress().toString();
        group.remove(channel);
        String path = null;
        for (Map.Entry<String, Channel> entry:clientChannelMap.entrySet()){
            if (entry.getValue().localAddress().toString().equals(string)){
                path = entry.getKey();
                break;
            }
        }
        if (!StringUtils.isEmpty(path)){
            clientChannelMap.remove(path);
        }
        channel.close();
    }


    private static final MessageProbuf.Message respSuccessVailMsg = MessageProbuf.Message.newBuilder()
                                .setType(MessageProbuf.MessageType.VALID).setExtend("SUCCESS").build();

    public static void sendVailSuccess(Channel channel){
        channel.writeAndFlush(respSuccessVailMsg);
    }

    public static void sendVailFail(Channel channel, String failCause){
        MessageProbuf.Message respFailVailMsg = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.VALID).setExtend(failCause).build();
        channel.writeAndFlush(respFailVailMsg).addListener(f->{
            channel.close();
        });
    }

}
