package com.github.fishlikewater.proxy;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import io.netty.util.ResourceLeakDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ProxyApplication implements InitializingBean{

   private final ProxyConfig proxyConfig;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        if(proxyConfig.getIsOpenCheckMemoryLeak()){
            System.setProperty("io.netty.leakDetection.maxRecords", "100");
            System.setProperty("io.netty.leakDetection.acquireAndReleaseOnly", "true");
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        }
    }
}
