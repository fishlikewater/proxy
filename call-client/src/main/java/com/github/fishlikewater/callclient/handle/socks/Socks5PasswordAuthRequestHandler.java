package com.github.fishlikewater.callclient.handle.socks;

import com.github.fishlikewater.callclient.config.ProxyConfig;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

	private final ProxyConfig proxyConfig;

	public Socks5PasswordAuthRequestHandler(ProxyConfig proxyConfig) {
		this.proxyConfig = proxyConfig;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
		log.debug("用户名密码 : " + msg.username() + "," + msg.password());
		if(msg.username().equals(proxyConfig.getUsername()) && msg.password().equals(proxyConfig.getPassword())) {
			//log.info("使用客户机验证成功");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
			ctx.writeAndFlush(passwordAuthResponse);
		} else {
			log.info("验证失败");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
			//发送鉴权失败消息，完成后关闭channel
			ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
