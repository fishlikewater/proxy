package com.github.fishlikewater.server.handle.socks;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fishlikewater@126.com
 * @since 2022年10月23日 15:35
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Socks5Constant {

    public static final AttributeKey<String> ACCOUNT = AttributeKey.valueOf("account");

    @Setter
    protected static Map<String, AtomicLong> accountFlow = new ConcurrentHashMap<>();

    @Setter
    @Getter
    protected static Map<String, String> accountMap = new ConcurrentHashMap<>();
}
