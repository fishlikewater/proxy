package com.github.fishlikewater.proxy;

import com.github.fishlikewater.proxy.boot.NettyDnsServer;
import com.github.fishlikewater.proxy.boot.NettyProxyServer;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ProxyApplication {

   @Autowired
   private ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @EventListener
    public void deployProxy(ApplicationReadyEvent event){
        if(proxyConfig.getType() == ProxyType.dns){
            System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
            System.setProperty("sun.net.spi.nameservice.nameservers", proxyConfig.getProxyDns());
            System.setProperty("sun.net.spi.nameservice.provider.2", "default");
            new NettyDnsServer(proxyConfig).start();
        }else{
            new NettyProxyServer(proxyConfig).start();
        }
    }
}
