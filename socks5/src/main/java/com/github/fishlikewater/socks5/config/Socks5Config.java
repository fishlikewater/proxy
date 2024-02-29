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
package com.github.fishlikewater.socks5.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("socks")
@Data
@Component
public class Socks5Config {

    private int port;

    private String address;

    /**
     * 验证用户名(客户端用户名)
     */
    private String username;

    /**
     * 验证密码
     */
    private String password;

    /**
     * 是否开启验证
     */
    private boolean auth;

    /**
     * 是否检测 能否连接(完全使用socks模式，连接前先发送到目标客户端，目标客户端连接目标地址，连接成功后返回) 如果为false则快速返回连接成功的状态
     */
    private boolean checkConnect;

    /**
     * 过滤网段 如配置192.168.12 则只有192.168.12开头的ip 的请求会流转到服务端,其他ip则会本地发起请求
     */
    private String filterIp;

    private Mapping[] mapping;

    @Data
    public static class Mapping {

        /**
         * 映射ip
         */
        private String mappingIp;

        /**
         * 请求ip
         */
        private String requestIp;

    }

}

