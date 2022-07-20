package com.github.fishlikewater.proxy.handler.socks;

import com.github.fishlikewater.proxy.handler.BootStrapFactroy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Socks5ClientCommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

	private Bootstrap bootstrap;

	private ChannelFuture future;

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
		log.info("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
		if (msg.type().equals(Socks5CommandType.BIND)){
			log.info("分配的ip为:{}", msg.dstAddr());
		}

		if(msg.type().equals(Socks5CommandType.CONNECT)) {
			if(bootstrap != null){
				future.await();
				future.channel().writeAndFlush(msg);
			}
			log.trace("准备连接目标服务器");
			bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
			log.trace("连接目标服务器");
			future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(final ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						log.trace("成功连接目标服务器");
						if(ctx.pipeline().get(Socks5ClientCommandRequestHandler.class) != null){
							ctx.pipeline().remove(Socks5ClientCommandRequestHandler.class);
						}
						ctx.pipeline().addLast(new Client2DestHandler(future));
						future.channel().pipeline().addLast(new Dest2ClientHandler(ctx));
						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
						ctx.writeAndFlush(commandResponse);
					} else {
						log.debug("连接目标服务器失败");
						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
						ctx.writeAndFlush(commandResponse);
					}
				}

			});
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) {
			// 远程主机强迫关闭了一个现有的连接的异常
			ctx.close();
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}

	/**
	 * 将目标服务器信息转发给客户端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelHandlerContext clientChannelContext;
		
		public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
			this.clientChannelContext = clientChannelContext;
		}

		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			boolean canWrite = ctx.channel().isWritable();
			log.trace(ctx.channel() + " 可写性：" + canWrite);
			//流量控制，不允许继续读
			clientChannelContext.channel().config().setAutoRead(canWrite);
			super.channelWritabilityChanged(ctx);
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
			log.trace("将目标服务器信息转发给客户端");
			clientChannelContext.writeAndFlush(destMsg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
			log.trace("目标服务器断开连接");
			clientChannelContext.channel().close();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (cause instanceof IOException) {
				// 远程主机强迫关闭了一个现有的连接的异常
				ctx.close();
			} else {
				super.exceptionCaught(ctx, cause);
			}
		}
	}
	
	/**
	 * 将客户端的消息转发给目标服务器端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Client2DestHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelFuture destChannelFuture;
		
		public Client2DestHandler(ChannelFuture destChannelFuture) {
			this.destChannelFuture = destChannelFuture;
		}

		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			boolean canWrite = ctx.channel().isWritable();
			log.trace(ctx.channel() + " 可写性：" + canWrite);
			//流量控制，不允许继续读
			destChannelFuture.channel().config().setAutoRead(canWrite);
			super.channelWritabilityChanged(ctx);
		}


		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			log.trace("将客户端的消息转发给目标服务器端");
			destChannelFuture.channel().writeAndFlush(msg);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.trace("客户端断开连接");
			destChannelFuture.channel().close();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (cause instanceof IOException) {
				// 远程主机强迫关闭了一个现有的连接的异常
				ctx.close();
			} else {
				super.exceptionCaught(ctx, cause);
			}
		}
	}
}
