package com.github.fishlikewater.proxyp2p.call;

import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月18日 16:01
 **/
@Slf4j
public class SocksServerBoot extends Server{


    public SocksServerBoot(CallConfig callConfig) {
        setCallConfig(callConfig);
    }

    public void start() {
        final ServerBootstrap serverBootstrap = BootStrapFactroy.getServerBootstrap();
        config(serverBootstrap);
        serverBootstrap.childHandler(new SocksInitializer(getCallConfig()));
        try {
            Channel ch = serverBootstrap.bind(getCallConfig().getSocksAddress(), getCallConfig().getSocksProt()).sync().channel();
            log.info("⬢ start server this port:{} and adress:{} proxy type:{}", getCallConfig().getSocksProt(), getCallConfig().getSocksAddress(), "socks");
            ch.closeFuture().addListener(t -> log.info("⬢  {}服务开始关闭", "socks"));
        } catch (InterruptedException e) {
            log.error("⬢ start server fail", e);
        }
    }

}
