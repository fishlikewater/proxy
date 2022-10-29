package com.github.fishlikewater.proxyp2p;

import com.github.fishlikewater.proxyp2p.config.ServerConfig;
import com.github.fishlikewater.proxyp2p.server.UdpServerBoot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ProxyP2pApplication implements CommandLineRunner {

    private final ServerConfig serverConfig;

    public static void main(String[] args) {
        SpringApplication.run(ProxyP2pApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final UdpServerBoot udpServerBoot = new UdpServerBoot(serverConfig);
        udpServerBoot.start();
    }
}
