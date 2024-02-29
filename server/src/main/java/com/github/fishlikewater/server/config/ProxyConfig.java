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
package com.github.fishlikewater.server.config;

import com.github.fishlikewater.config.ProxyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Objects;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {

    private int next = 0;

    /**
     * 是否启动udp服务
     */
    private boolean isStartUdp;

    /**
     * udp服务端口
     */
    private int udpPort;

    /**
     * 服务启动列表
     */
    private ProxyType[] type;

    /**
     * 监控地址
     */
    private String address;

    /**
     * 监控端口
     */
    private int port;

    /**
     * token
     */
    private String token;

    /**
     * 心跳检测间隔
     */
    private long timeout;

    /**
     * 是否使用netty日志处理器
     */
    private boolean logging;

    /**
     * 是否开启验证
     */
    private boolean auth;

    private String socksName;

    private String socksPassWord;

    /**
     * 代理路劲
     */
    private String proxyPath;

    /**
     * 每一帧最大字节
     */
    private DataSize maxFrameLength = DataSize.ofBytes(5 * 1024 * 1024L);

    private int[] localPorts;

    /**
     * 是否固定本地链接远程使用的端口
     */
    private boolean useLocalPorts;

    private String ipPrefix = "192.168.12.";


    public int getOneLocalPort() {
        if (Objects.isNull(localPorts)) {
            return 0;
        }
        if (next >= localPorts.length) {
            next = 0;
        }
        int localPort = localPorts[next];
        next++;
        return localPort;
    }
}

