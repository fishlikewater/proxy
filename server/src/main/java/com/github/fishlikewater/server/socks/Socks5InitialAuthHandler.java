package com.github.fishlikewater.server.socks;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5InitialAuthHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

	private boolean isAuth;
	
	public Socks5InitialAuthHandler(boolean isAuth) {
		this.isAuth = isAuth;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
		log.debug("初始化ss5连接 : " + msg);
		if(msg.decoderResult().isFailure()) {
			log.debug("不是ss5协议");
			ctx.fireChannelRead(msg);
		} else {
			if(msg.version().equals(SocksVersion.SOCKS5)) {
				if(isAuth) {
					//log.info("需验证");
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
					ctx.writeAndFlush(initialResponse);
				} else {
					log.debug("不需验证");
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
					ctx.writeAndFlush(initialResponse);
				}
			}
		}
	}

}
