package com.github.fishlikewater.proxy.kit;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @date: 2022年07月19日 14:28
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
public class IpCacheKit {

    @Getter
    private static final ConcurrentHashMap<String, Channel> ipsMap = new ConcurrentHashMap<>();

    public static void add(String ip, Channel channel) {
        ipsMap.put(ip, channel);
    }

    public static boolean findByIp(String ip) {
        return ipsMap.containsKey(ip);
    }

    public static void remove(Channel channel) {
        final String longText = channel.id().asLongText();
        String ip = "";
        for (Map.Entry<String, Channel> entry : ipsMap.entrySet()) {
            if (entry.getValue().id().asLongText().equals(longText)) {
                ip = entry.getKey();
                break;
            }
        }
        if (StrUtil.isNotBlank(ip)) {
            ipsMap.remove(ip);
        }
    }
}
