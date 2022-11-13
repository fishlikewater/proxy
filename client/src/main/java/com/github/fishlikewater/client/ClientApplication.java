package com.github.fishlikewater.client;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ChannelKit;
import com.github.fishlikewater.config.ProxyType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class ClientApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private ProxyClient proxyClient;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (proxyConfig.getProxyType() == ProxyType.http){
            final List<ProxyConfig.HttpMapping> httpMappings = proxyConfig.getHttpMappings();
            for (ProxyConfig.HttpMapping httpMapping : httpMappings) {
                ChannelKit.getHTTP_MAPPING_MAP().put(httpMapping.getName(), httpMapping);
            }
        }
        proxyClient = new ProxyClient(proxyConfig);
        proxyClient.run();

    }

    @Override
    public void destroy() {
        if (proxyClient != null){
            proxyClient.stop();
        }
    }
}
