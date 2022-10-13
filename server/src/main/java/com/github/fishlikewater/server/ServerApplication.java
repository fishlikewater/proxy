package com.github.fishlikewater.server;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.ProxyServer;
import com.github.fishlikewater.server.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ServerApplication implements CommandLineRunner {

    private final ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            if (proxyType == ProxyType.proxy_server || proxyType == ProxyType.socks
                    || proxyType == ProxyType.proxy_server_http || proxyType == ProxyType.http) {
                new ProxyServer(proxyConfig, proxyType).start();
            }
        }
    }
}
