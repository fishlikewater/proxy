package com.github.fishlikewater.proxyp2p.call;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
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
public class CallKit {

    @Setter
    public static InetSocketAddress p2pInetSocketAddress;

    @Setter
    public static Channel channel;

    public final static AttributeKey<String> LOCAL_INFO = AttributeKey.newInstance("LOCAL_INFO");

    @Getter
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

}
