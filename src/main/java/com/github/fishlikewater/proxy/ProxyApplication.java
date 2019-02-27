package com.github.fishlikewater.proxy;

import com.github.fishlikewater.proxy.boot.NettyProxyServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ProxyApplication {

    @Value("${proxy.address}")
    private String address;
    @Value("${proxy.port:8080}")
    private int port;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @EventListener
    public void deployProxy(ApplicationReadyEvent event){
       new NettyProxyServer().start(address, port);
    }
}
