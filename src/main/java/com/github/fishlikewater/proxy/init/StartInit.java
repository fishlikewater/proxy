package com.github.fishlikewater.proxy.init;

import com.github.fishlikewater.proxy.boot.HttpProxyServer;
import com.github.fishlikewater.proxy.boot.NettyUdpServer;
import com.github.fishlikewater.proxy.boot.TcpProxyClient;
import com.github.fishlikewater.proxy.boot.TcpProxyServer;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月01日 9:20
 **/
@Component
@RequiredArgsConstructor
public class StartInit implements CommandLineRunner {

    private final ProxyConfig proxyConfig;

    @Override
    public void run(String... args) throws Exception {
        final ProxyType[] type = proxyConfig.getType();
        for (ProxyType proxyType : type) {
            if (proxyType == ProxyType.proxy_server || proxyType == ProxyType.socks || proxyType == ProxyType.tcp_server){
                new TcpProxyServer(proxyConfig, proxyType).start();
            }
            if (proxyType == ProxyType.proxy_client || proxyType == ProxyType.tcp_client){
                new TcpProxyClient(proxyConfig, proxyType).run();
            }
            if (proxyType == ProxyType.http || proxyType == ProxyType.proxy_server_http){
                new HttpProxyServer(proxyConfig, proxyType).start();
            }
            if (proxyType == ProxyType.dns){
                System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
                System.setProperty("sun.net.spi.nameservice.nameservers", proxyConfig.getProxyDns());
                System.setProperty("sun.net.spi.nameservice.provider.2", "default");
                new NettyUdpServer(proxyConfig, proxyType).start();
            }

        }
    }
}
