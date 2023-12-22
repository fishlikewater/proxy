package com.github.fishlikewater.server.handle.socks;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fishlikewater@126.com
 * @since 2022年10月23日 15:35
 **/
@Slf4j
public class Socks5InitialAuthHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

    private final boolean isAuth;

    public Socks5InitialAuthHandler(boolean isAuth) {
        this.isAuth = isAuth;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) {
        log.debug("初始化ss5连接 : " + msg);
        if (msg.decoderResult().isFailure()) {
            log.debug("不是ss5协议");
            ctx.fireChannelRead(msg);
        } else {
            if (msg.version().equals(SocksVersion.SOCKS5)) {
                Socks5InitialResponse initialResponse;
                if (isAuth) {
                    initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
                } else {
                    initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
                }
                ctx.writeAndFlush(initialResponse);
            }
        }
    }

}
