package com.github.fishlikewater.server.kit;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月09日 17:16
 **/
@Slf4j
public class ChannelGroupKit {


    /**目标机路径属性 */
    public static final AttributeKey<String> CLIENT_PATH = AttributeKey.valueOf("client_path");

    public static final AttributeKey<Channel> DATA_CHANNEL = AttributeKey.valueOf("data_channel");

    /**请求机绑定目标机属性*/
    public static final AttributeKey<Channel> CALL_REMOTE_CLIENT = AttributeKey.valueOf("call_remote_client");

    /**目标主机绑定请求机*/
    public static final AttributeKey<Channel> CALL_REQUEST_CLIENT = AttributeKey.valueOf("call_request_client");

    /**客户端类型属性*/
    public static final AttributeKey<String> CLIENT_TYPE = AttributeKey.valueOf("client_type");

    /**连接分配的虚拟ip */
    public static final AttributeKey<String> VIRT_IP = AttributeKey.valueOf("virt_ip");

    @Getter
    private static final ConcurrentHashMap<String, Channel> CLIENT_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void add(String path, Channel channel){
        CLIENT_CHANNEL_MAP.put(path, channel);
    }
    public static void remove(String path){
        CLIENT_CHANNEL_MAP.remove(path);
    }
    public static Channel find(String path){
        return CLIENT_CHANNEL_MAP.get(path);
    }

}
