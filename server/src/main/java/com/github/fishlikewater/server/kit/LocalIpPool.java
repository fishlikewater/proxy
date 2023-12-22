package com.github.fishlikewater.server.kit;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * <p>
 * 默认ip池实现
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 10:55
 **/
public class LocalIpPool implements IpPool {

    private final static ConcurrentLinkedDeque<Integer> IP_POOL = new ConcurrentLinkedDeque<>();
    private final static int MAX_IP = 255;

    static {
        for (int i = 1; i < MAX_IP; i++) {
            IP_POOL.add(i);
        }
    }

    @Override
    public Integer getIp() {
        return IP_POOL.poll();
    }

    @Override
    public void retrieve(int ip) {
        IP_POOL.add(ip);
    }

    @Override
    public void remove(int ip) {
        IP_POOL.remove(ip);
    }


}
