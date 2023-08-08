package com.github.fishlikewater.server;

import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.Server;
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

    private final List<Server> servers = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            final Server proxyServer = new Server(proxyConfig, proxyType);
            proxyServer.start();
            servers.add(proxyServer);
        }
    }

    @Override
    public void destroy() {
        servers.forEach(Server::stop);
    }
}
