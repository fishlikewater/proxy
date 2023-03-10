package com.github.fishlikewater.server.handle.socks;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author fishlikewater@126.com
 * @since 2022年10月23日 15:35
 **/
@Slf4j
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {


	public Socks5PasswordAuthRequestHandler() {
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) {
		final String pass = Socks5Constant.getAccountMap().get(msg.username());
		if (StrUtil.isNotBlank(pass) && StrUtil.equals(pass, msg.password())){
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
			ctx.writeAndFlush(passwordAuthResponse);
			ctx.channel().attr(Socks5Constant.ACCOUNT).set(msg.username());
		} else {
			log.warn("验证失败");
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
			//发送鉴权失败消息，完成后关闭channel
			ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}

	public static void main(String[] args) throws IOException {
		final Map<String, String> map = JSON.parseObject(new FileInputStream(FileUtil.file("account.json")), Map.class);
		System.out.println(map);
	}

}
