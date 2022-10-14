package com.github.fishlikewater.callcleint;

import com.github.fishlikewater.callcleint.boot.ProxyClient;
import com.github.fishlikewater.callcleint.boot.TcpServer;
import com.github.fishlikewater.callcleint.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class CallCleintApplication implements CommandLineRunner {

    private final ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(CallCleintApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        new ProxyClient(proxyConfig).run();
        final ProxyConfig.Mapping[] mappings = proxyConfig.getMappings();
        for (ProxyConfig.Mapping mapping : mappings) {
            new TcpServer(proxyConfig, mapping).start();
        }
    }
}
