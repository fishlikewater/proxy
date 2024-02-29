/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static final ConcurrentLinkedDeque<Integer> IP_POOL = new ConcurrentLinkedDeque<>();
    private static final int MAX_IP = 255;

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
