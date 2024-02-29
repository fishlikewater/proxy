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
package com.github.fishlikewater.server;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.ServerTcp;
import com.github.fishlikewater.server.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fishl
 */
@SpringBootApplication
@RequiredArgsConstructor
public class ServerApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private final List<ServerTcp> serverTcps = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            final ServerTcp proxyServer = new ServerTcp(proxyConfig, proxyType);
            proxyServer.start();
            serverTcps.add(proxyServer);
        }
    }

    @Override
    public void destroy() {
        serverTcps.forEach(ServerTcp::stop);
    }
}
