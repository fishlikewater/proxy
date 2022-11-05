package com.github.fishlikewater.proxyp2p.client;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月05日 9:19
 **/
@Data
public class ClientKit {

    @Setter
    public static InetSocketAddress p2pInetSocketAddress;

    @Setter
    public static Channel channel;

    @Getter
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

}
