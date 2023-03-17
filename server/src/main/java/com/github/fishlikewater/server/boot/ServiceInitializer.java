package com.github.fishlikewater.server.boot;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.server.config.BootModel;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.ServerHeartBeatHandler;
import com.github.fishlikewater.server.handle.myprotocol.AuthHandler;
import com.github.fishlikewater.server.handle.myprotocol.MyProtocolHandler;
import com.github.fishlikewater.server.handle.myprotocol.RegisterHandler;
import com.github.fishlikewater.server.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.server.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.server.handle.socks.Socks5PasswordAuthRequestHandler;
import com.github.fishlikewater.server.handle.vpn.VpnMessageHandler;
import com.github.fishlikewater.server.handle.vpn.VpnRegisterHandler;
import com.github.fishlikewater.server.kit.DefaultConnectionValidate;
import com.github.fishlikewater.server.kit.IpMapping;
import com.github.fishlikewater.server.kit.IpPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年02月26日 21:47
 **/
@Slf4j
public class ServiceInitializer extends ChannelInitializer<Channel> {

    private final ProxyConfig proxyConfig;
    private final ProxyType proxyType;
    private final IpMapping ipMapping;
    private final IpPool ipPool;

    public ServiceInitializer(ProxyConfig proxyConfig, ProxyType proxyType) {
        log.info("init handler");
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
        this.ipMapping = ServiceLoaderUtil.loadFirst(IpMapping.class);
        this.ipPool = ServiceLoaderUtil.loadFirst(IpPool.class);

    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS));
        p.addLast(new ServerHeartBeatHandler());
        /* 是否打开日志*/
        if (proxyConfig.isLogging()) {
            p.addLast(new LoggingHandler());
        }
       if (proxyType == ProxyType.socks) {
            p.addFirst(new Socks5CommandRequestDecoder());
            if (proxyConfig.isAuth()) {
                /* 添加验证机制*/
                p.addFirst(new Socks5PasswordAuthRequestHandler());
                p.addFirst(new Socks5PasswordAuthRequestDecoder());
            }
            p.addFirst(new Socks5InitialAuthHandler(proxyConfig.isAuth()));
            p.addFirst(Socks5ServerEncoder.DEFAULT);
            p.addFirst(new Socks5InitialRequestDecoder());
            /* Socks connection handler */
            p.addLast(new Socks5CommandRequestHandler(proxyConfig));

        } else if (proxyType == ProxyType.proxy_server && proxyConfig.getBootModel() == BootModel.ONE_TO_ONE) {
            p
                    .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                    .addLast(new MyByteToMessageCodec())
                    .addLast(new AuthHandler(new DefaultConnectionValidate(), proxyConfig))
                    .addLast(new RegisterHandler())
                    .addLast(new MyProtocolHandler());
        }else if (proxyType == ProxyType.proxy_server && proxyConfig.getBootModel() == BootModel.VPN){
           p
                   .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                   .addLast(new MyByteToMessageCodec())
                   .addLast(new AuthHandler(new DefaultConnectionValidate(), proxyConfig))
                   .addLast(new VpnRegisterHandler(ipMapping, ipPool, proxyConfig))
                   .addLast(new VpnMessageHandler(ipMapping));
       }


    }
}
