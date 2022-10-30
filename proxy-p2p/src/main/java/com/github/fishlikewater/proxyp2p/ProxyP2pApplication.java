package com.github.fishlikewater.proxyp2p;

import com.github.fishlikewater.proxyp2p.client.UdpCientBoot;
import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.config.ServerConfig;
import com.github.fishlikewater.proxyp2p.server.UdpServerBoot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ProxyP2pApplication implements CommandLineRunner, DisposableBean {

    @Value("${spring.profiles.active}")
    private String env;

    private UdpServerBoot udpServerBoot;
    private UdpCientBoot udpCientBoot;

    private final ServerConfig serverConfig;
    private final ClientConfig clientConfig;

    public static void main(String[] args) {
        SpringApplication.run(ProxyP2pApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (env.equals("server")){
            udpServerBoot = new UdpServerBoot(serverConfig);
            udpServerBoot.start();
        }

        if (env.equals("client")){
            udpCientBoot = new UdpCientBoot(clientConfig);
            udpCientBoot.start();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (udpServerBoot != null){
            udpServerBoot.stop();
        }
        if (udpCientBoot != null){
            udpCientBoot.stop();
        }
    }
}
