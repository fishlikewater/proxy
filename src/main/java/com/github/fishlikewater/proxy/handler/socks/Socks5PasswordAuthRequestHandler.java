package com.github.fishlikewater.proxy.handler.socks;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.IpCacheKit;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

	private final ProxyConfig proxyConfig;

	private static final AtomicInteger ips = new AtomicInteger(1);

	public Socks5PasswordAuthRequestHandler(ProxyConfig proxyConfig) {
		this.proxyConfig = proxyConfig;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
		log.debug("用户名密码 : " + msg.username() + "," + msg.password());
		if (msg.username().equals(proxyConfig.getClientUsername()) && msg.password().equals(proxyConfig.getPassword())){
			//log.info("目标机器验证成功");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
			ctx.writeAndFlush(passwordAuthResponse);
		} else if(msg.username().equals(proxyConfig.getUsername()) && msg.password().equals(proxyConfig.getPassword())) {
			//log.info("使用客户机验证成功");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
			ctx.writeAndFlush(passwordAuthResponse);
			ChannelGroupKit.add(ctx.channel());
		} else {
			log.info("验证失败");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
			//发送鉴权失败消息，完成后关闭channel
			ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
