package com.github.fishlikewater.proxy.handler;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import com.github.fishlikewater.proxy.handler.dns.DnsServerHandler;
import com.github.fishlikewater.proxy.handler.p2p.P2pDecode;
import com.github.fishlikewater.proxy.handler.p2p.P2pEncode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月23日 8:59
 * @since
 **/
@Slf4j
public class ProxyUdpInitializer extends ChannelInitializer<Channel> {

    private ProxyConfig proxyConfig;

    public ProxyUdpInitializer(ProxyConfig proxyConfig) {
        log.info("init handler");
        this.proxyConfig = proxyConfig;
    }


    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if(proxyConfig.getType() == ProxyType.dns){
            p.addFirst("dnsHandler", new DnsServerHandler());
        }
        else if(proxyConfig.getType() == ProxyType.p2p){
            p.addLast("p2pencode", new P2pEncode());
            p.addLast("lengthEncode", new LengthFieldPrepender(4, false));
            p.addLast("p2pdeode", new P2pDecode());
            p.addLast("lengthDecode", new LengthFieldBasedFrameDecoder(60553, 0, 4,0, 4, true));
        }
    }
}
