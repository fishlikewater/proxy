package com.github.fishlikewater.callclient;

import com.github.fishlikewater.socks5.config.Socks5Config;
import com.github.fishlikewater.callclient.boot.ProxyClient;
import com.github.fishlikewater.callclient.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fishlikewater@126.com
 * @since 2023/3/10 10:20
 */

@SpringBootApplication
@RequiredArgsConstructor
public class CallClientApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;
    private final Socks5Config socks5Config;

    private ProxyClient proxyClient;

    public static void main(String[] args) {
        SpringApplication.run(CallClientApplication.class, args);
    }

    @Override
    public void run(String... args) {

        proxyClient = new ProxyClient(proxyConfig, socks5Config);
        proxyClient.run();
    }

    @Override
    public void destroy() {
        if (proxyClient != null){
            proxyClient.stop();
        }
    }
}
