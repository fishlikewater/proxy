package com.github.fishlikewater.proxyp2p.kit;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月30日 15:58
 **/
public class ServerKit {

    @Getter
    public static final Map<String, InetSocketAddress> ADDRESS_MAP = new ConcurrentHashMap<>();

}
