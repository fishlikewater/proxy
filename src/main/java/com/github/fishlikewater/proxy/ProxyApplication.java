package com.github.fishlikewater.proxy;

import com.github.fishlikewater.proxy.boot.NettyProxyClient;
import com.github.fishlikewater.proxy.boot.NettyProxyServer;
import com.github.fishlikewater.proxy.boot.NettySocks5ProxyClient;
import com.github.fishlikewater.proxy.boot.NettyUdpServer;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import io.netty.util.ResourceLeakDetector;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProxyApplication implements InitializingBean, DisposableBean{

    private NettyProxyServer nettyProxyServer1;
    private NettyProxyServer nettyProxyServer2;
    private NettyProxyClient nettyProxyClient1;
    private NettyUdpServer nettyUdpServer;

   @Autowired
   private ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }


    @Override
    public void destroy() throws Exception {
        if(nettyProxyServer1 != null){
            nettyProxyServer1.stop();
        }
        if(nettyProxyServer2 != null){
            nettyProxyServer2.stop();
        }
        if(nettyProxyClient1 != null){
            nettyProxyClient1.stop();
        }
        if(nettyUdpServer != null){
            nettyUdpServer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(proxyConfig.getIsOpenCheckMemoryLeak()){
            System.setProperty("io.netty.leakDetection.maxRecords", "100");
            System.setProperty("io.netty.leakDetection.acquireAndReleaseOnly", "true");
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        }
        ProxyType type = proxyConfig.getType();
        if(type == ProxyType.dns){
            /*if(proxyConfig.getProxyDns() != null){
                System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
                System.setProperty("sun.net.spi.nameservice.nameservers", proxyConfig.getProxyDns());
                System.setProperty("sun.net.spi.nameservice.provider.2", "default");
            }*/
            nettyUdpServer = new  NettyUdpServer(proxyConfig);
            nettyUdpServer.start();
            return;
        }else if(type == ProxyType.proxy_server){
            ProxyConfig proxyHttpConfig = new ProxyConfig();
            BeanUtils.copyProperties(proxyConfig, proxyHttpConfig);
            proxyHttpConfig.setType(ProxyType.proxy_server_http);
            nettyProxyServer2 = new NettyProxyServer(proxyHttpConfig);
            nettyProxyServer2.start();
        }
        proxyConfig.setType(type);
        if(type == ProxyType.proxy_server || type ==  ProxyType.http || type == ProxyType.socks){
            nettyProxyServer1 = new NettyProxyServer(proxyConfig);
            nettyProxyServer1.start();
        }
        if(type == ProxyType.proxy_client){
            nettyProxyClient1 = new NettyProxyClient(proxyConfig);
            nettyProxyClient1.run();
        }
        if(type == ProxyType.socks_client){
            nettyProxyClient1 = new NettySocks5ProxyClient(proxyConfig);
            nettyProxyClient1.run();
        }
    }
}
