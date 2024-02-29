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


import io.netty.channel.Channel;

/**
 * <p>
 * ip 映射
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月16日 15:43
 **/
public interface IpMapping {

    /**
     * 添加映射关系
     *
     * @param ip      分配的ip
     * @param channel 对应的连接
     * @author fishlikewater@126.com
     * @since 2023/3/16 15:56
     */
    void put(String ip, Channel channel);


    /**
     * 获取连接
     *
     * @param ip 分配的ip
     * @return io.netty.channel.Channel
     * @author fishlikewater@126.com
     * @since 2023/3/16 16:01
     */
    Channel getChannel(String ip);


    /**
     * 清除映射关系
     *
     * @param ip 分配的ip
     * @author fishlikewater@126.com
     * @since 2023/3/16 15:56
     */
    void remove(String ip);


}
