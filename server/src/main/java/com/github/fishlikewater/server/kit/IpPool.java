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

/**
 * <p>
 * ip池
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 10:53
 **/
public interface IpPool {

    /**
     * 获取一个未使用的ip
     *
     * @return java.lang.Integer
     * @author fishlikewater@126.com
     * @since 2023/3/17 10:59
     */
    Integer getIp();

    /**
     * 回收地址
     *
     * @param ip 地址
     * @author fishlikewater@126.com
     * @since 2023/3/17 11:01
     */
    void retrieve(int ip);

    /**
     * 删除ip
     *
     * @param ip 地址
     * @author fishlikewater@126.com
     * @since 2023/3/17 11:01
     */
    void remove(int ip);
}
