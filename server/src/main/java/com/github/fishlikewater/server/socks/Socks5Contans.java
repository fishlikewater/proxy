package com.github.fishlikewater.server.socks;

import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月23日 15:35
 **/
public class Socks5Contans {

    public static final AttributeKey<String> ACCOUNT = AttributeKey.valueOf("account");

    public static final Map<String, AtomicLong> accountFlow = new ConcurrentHashMap<>();
}
