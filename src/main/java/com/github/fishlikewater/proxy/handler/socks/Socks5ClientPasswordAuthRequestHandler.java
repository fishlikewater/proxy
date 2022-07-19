package com.github.fishlikewater.proxy.handler.socks;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5ClientPasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthResponse> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthResponse msg) throws Exception {
		if (msg.status().isSuccess()){
			log.info("服务器验证成功");
		}else {
			log.info("服务器验证失败");
		}
	}
}
