package com.github.fishlikewater.proxy.kit;

import io.netty.bootstrap.Bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName BootClientPool
 * @Description
 * @Date 2019年04月05日 9:56
 * @since
 **/
public class BootClientPool {

    private Map<String, Bootstrap> pool = new ConcurrentHashMap<String, Bootstrap>(1024);

    private static final BootClientPool INS = new BootClientPool();

    public static BootClientPool single() {
        return INS;
    }

    public void put(String key, Bootstrap bootstrap){
        if(!pool.containsKey(key)){
            pool.put(key,bootstrap);
        }
    }

    public Bootstrap get(String key){
        if(pool.containsKey(key)){
            return pool.get(key);
        }
        return null;
    }

    public void remove(String key){
        if(pool.containsKey(key)){
            pool.remove(key);
        }
    }


}
