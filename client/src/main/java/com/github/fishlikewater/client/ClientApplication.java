package com.github.fishlikewater.client;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.config.ProxyType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ClientApplication implements CommandLineRunner {

    private final ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            if (proxyType == ProxyType.proxy_client || proxyType == ProxyType.tcp_client) {
                new ProxyClient(proxyConfig, proxyType).run();
            }
        }

    }
}
