package com.github.fishlikewater.server;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.boot.ProxyServer;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.PassWordCheck;
import com.github.fishlikewater.server.handle.socks.Socks5Contans;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
public class ServerApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private final List<ProxyServer> servers = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            if (proxyType == ProxyType.http){
                PassWordCheck.setUsername(proxyConfig.getUsername());
                PassWordCheck.setPassword(proxyConfig.getPassword());
            }
            if (proxyType == ProxyType.socks){
                final Map<String, String> map = JSON.parseObject(new FileInputStream(FileUtil.file("account.json")),  Map.class);
                Socks5Contans.setAccountMap(map);
                //final Map<String, String> fowMap = JSONObject.parseObject(new FileInputStream(FileUtil.file("flow.json")),  Map.class);
                //Socks5Contans.setAccountFlow(fowMap);
            }
            if (proxyType == ProxyType.proxy_server || proxyType == ProxyType.socks
                    || proxyType == ProxyType.proxy_server_http || proxyType == ProxyType.http) {
                final ProxyServer proxyServer = new ProxyServer(proxyConfig, proxyType);
                proxyServer.start();
                servers.add(proxyServer);
            }
        }
    }

    @Override
    public void destroy() {
        servers.forEach(ProxyServer::stop);
        /*final Map<String, AtomicLong> accountFlow = Socks5Contans.accountFlow;
        final String jsonString = JSON.toJSONString(accountFlow);
        FileWriter fileWriter = new FileWriter("flow.json");
        fileWriter.write(jsonString);*/
    }
}
