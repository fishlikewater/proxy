package com.github.fishlikewater.server.kit;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  默认ip映射关系实现
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月16日 15:57
 **/
public class LocalIpMapping implements IpMapping {

    private final ConcurrentHashMap<String, Channel> ipMapping = new ConcurrentHashMap<>();

    @Override
    public void put(String ip, Channel channel) {
        channel.closeFuture().addListener(future -> {
            if (future.isSuccess()){
                remove(ip);
            }
        });
        ipMapping.put(ip, channel);

    }

    @Override
    public Channel getChannel(String ip) {
        return ipMapping.get(ip);
    }

    @Override
    public void remove(String ip) {
        ipMapping.remove(ip);
    }
}
