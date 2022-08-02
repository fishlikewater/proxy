package com.github.fishlikewater.proxy.handler.socks_client;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @date: 2022年07月18日 15:08
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
@RequiredArgsConstructor
public class Socks5ClientInitialAuthHandler  extends SimpleChannelInboundHandler<Socks5InitialResponse> {

    private final ProxyConfig proxyConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5InitialResponse msg) throws Exception {
        final Socks5AuthMethod socks5AuthMethod = msg.authMethod();
        if (socks5AuthMethod == Socks5AuthMethod.PASSWORD){
            final DefaultSocks5PasswordAuthRequest socks5PasswordAuthRequest = new DefaultSocks5PasswordAuthRequest(proxyConfig.getClientUsername(), proxyConfig.getPassword());
            ctx.writeAndFlush(socks5PasswordAuthRequest);
        }else {
            log.info("链接成功, 不需要验证");
        }
    }
}
