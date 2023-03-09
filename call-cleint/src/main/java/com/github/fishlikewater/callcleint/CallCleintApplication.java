package com.github.fishlikewater.callcleint;

import cn.hutool.core.bean.BeanUtil;
import com.github.fishlikewater.callcleint.boot.ProxyClient;
import com.github.fishlikewater.callcleint.boot.SocksAbstractServerBoot;
import com.github.fishlikewater.callcleint.config.ProxyConfig;
import com.github.fishlikewater.callcleint.handle.ChannelKit;
import com.github.fishlikewater.config.ProxyType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class CallCleintApplication implements CommandLineRunner, DisposableBean {

    private final ProxyConfig proxyConfig;

    private ProxyClient proxyClient;
    private SocksAbstractServerBoot socksServerBoot;

    public static void main(String[] args) {
        SpringApplication.run(CallCleintApplication.class, args);
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
