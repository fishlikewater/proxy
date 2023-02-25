package com.github.fishlikewater.server;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.ProxyServer;
import com.github.fishlikewater.server.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class ServerApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private final List<ProxyServer> servers = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            final ProxyServer proxyServer = new ProxyServer(proxyConfig, proxyType);
            proxyServer.start();
            servers.add(proxyServer);
        }
    }

    @Override
    public void destroy() {
        servers.forEach(ProxyServer::stop);
    }
}
