package com.github.fishlikewater.proxy.kit;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月09日 17:16
 * @since
 **/
@Slf4j
public class ChannelGroupKit {

    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Getter
    private static ConcurrentHashMap<String, Channel> clientChannelMap = new ConcurrentHashMap<>();

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
        group.remove(channel);
    }
}
