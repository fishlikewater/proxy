package com.github.fishlikewater.callclient;

import cn.hutool.core.bean.BeanUtil;
import com.github.fishlikewater.callclient.boot.ProxyClient;
import com.github.fishlikewater.callclient.boot.SocksAbstractServerBoot;
import com.github.fishlikewater.callclient.config.ProxyConfig;
import com.github.fishlikewater.callclient.handle.ChannelKit;
import com.github.fishlikewater.config.ProxyType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

/**
 * @author fishlikewater@126.com
 * @since 2023/3/10 10:20
 */

@SpringBootApplication
@RequiredArgsConstructor
public class CallClientApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private ProxyClient proxyClient;
    private SocksAbstractServerBoot socksServerBoot;

    public static void main(String[] args) {
        SpringApplication.run(CallClientApplication.class, args);
    }

    @Override
    public void run(String... args) {

        final List<ProxyConfig.ProxyMapping> proxyMappings = proxyConfig.getProxyMappings();
        for (ProxyConfig.ProxyMapping proxyMapping : proxyMappings) {
            ChannelKit.getPROXY_MAPPING_MAP().put(proxyMapping.getDomain(), proxyMapping.getIp());
        }
        proxyConfig.setProxyType(ProxyType.proxy_client);
        proxyClient = new ProxyClient(proxyConfig);
        proxyClient.run();
        final ProxyConfig proxyConfig2 = BeanUtil.toBean(this.proxyConfig, ProxyConfig.class);
        proxyConfig2.setProxyType(ProxyType.socks);
        socksServerBoot = new SocksAbstractServerBoot(proxyConfig2);
        socksServerBoot.start();
    }

    @Override
    public void destroy() {
        if (socksServerBoot != null){
            socksServerBoot.stop();
        }
        if (proxyClient != null){
            proxyClient.stop();
        }
    }
}
