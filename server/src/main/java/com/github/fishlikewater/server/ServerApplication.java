package com.github.fishlikewater.server;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.Server;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.socks.Socks5Contans;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void run(String... args) throws FileNotFoundException {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            if (proxyType == ProxyType.socks) {
                final Map<String, String> map = JSON.parseObject(new FileInputStream(FileUtil.file("account.json")), Map.class);
                Socks5Contans.setAccountMap(map);
            }
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
