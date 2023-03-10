package com.github.fishlikewater.server.handle.socks;

import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fishlikewater@126.com
 * @since 2022年10月23日 15:35
 **/
public class Socks5Constant {

    public static final AttributeKey<String> ACCOUNT = AttributeKey.valueOf("account");

    @Setter
    public static Map<String, AtomicLong> accountFlow = new ConcurrentHashMap<>();

    @Setter
    @Getter
    public static  Map<String, String> accountMap = new ConcurrentHashMap<>();
}
