package com.github.fishlikewater.callcleint.boot;

import com.github.fishlikewater.callcleint.config.ProxyConfig;
import com.github.fishlikewater.callcleint.handle.ProxyClientInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  socks 启动类
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年10月18日 16:01
 **/
@Slf4j
public class SocksAbstractServerBoot extends AbstractServer {


    public SocksAbstractServerBoot(ProxyConfig proxyConfig) {
        setProxyConfig(proxyConfig);
    }

    public void start() {
        final ServerBootstrap serverBootstrap = BootStrapFactory.getServerBootstrap();
        config(serverBootstrap);
        serverBootstrap.childHandler(new ProxyClientInitializer(getProxyConfig()));
        try {
            Channel ch = serverBootstrap.bind(getProxyConfig().getSocksAddress(), getProxyConfig().getSocksPort()).sync().channel();
            log.info("⬢ start server this port:{} and address:{} proxy type:{}", getProxyConfig().getSocksPort(), getProxyConfig().getSocksAddress(), getProxyConfig().getProxyType());
            ch.closeFuture().addListener(t -> log.info("⬢  {}服务开始关闭", getProxyConfig().getProxyType()));
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }
    }

}
