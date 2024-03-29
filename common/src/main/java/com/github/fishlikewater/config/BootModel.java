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
package com.github.fishlikewater.config;

/**
 * <p>启动模式:</p>
 * <p>1.一对一连接模式,客户端与目标机建立独立连接,控制目标机进行网络访问</p>
 * <p>2.类vpn模式,服务端给客户端分配虚拟ip,通过虚拟ip访问组网的其他客户端</p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 9:46
 **/
@Deprecated
public enum BootModel {
    //一对一连接模式
    ONE_TO_ONE,
    //类vpn模式
    VPN
}
