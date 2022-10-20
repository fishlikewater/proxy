package com.github.fishlikewater.server.kit;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月13日 12:58
 **/
public class CacheUtil {

    private  static Cache<String, Channel> cache =  CaffeineCacheBuilder.createCaffeineCacheBuilder()
            .limit(100)
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .buildCache();

    public static void init(Cache<String, Channel> cache){
        CacheUtil.cache = cache;
    }

    public static void put(String requestId, Channel channel, long ex){
        cache.put(requestId, channel, ex, TimeUnit.SECONDS);
    }

    public static void remove(String requestId){
        cache.remove(requestId);
    }

    public static Channel get(String requestId){
        return cache.get(requestId);
    }


    public static Cache<String, Channel> getCache(){
        return cache;
    }


}
