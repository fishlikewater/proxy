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
package com.github.fishlikewater.kit;

import io.netty.channel.epoll.Epoll;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpollKit {

    /**
     * 判断当前系统是否支持epoll
     *
     * @return boolean
     */
    public static boolean epollIsAvailable() {
        boolean available = Epoll.isAvailable();
        boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
        return available && linux;
    }
}
