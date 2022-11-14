package com.github.fishlikewater.callcleint.handle.socks;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.callcleint.handle.ChannelKit;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String requestId = ctx.channel().attr(ChannelKit.LOCAL_INFO).get();
        if (StrUtil.isNotBlank(requestId)){
            ChannelKit.getChannel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
            sendCloseInfo(ChannelKit.getChannel(), requestId);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
       super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
		log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            final String requestId = IdUtil.next();
            ctx.channel().attr(ChannelKit.LOCAL_INFO).set(requestId);
            final MessageProbuf.Socks.Builder socksBuilder = MessageProbuf.Socks.newBuilder();
            socksBuilder.setAddress(msg.dstAddr());
            socksBuilder.setPort(msg.dstPort());
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setScoks(socksBuilder.build())
                    .setRequestId(requestId)
                    .setProtocol(MessageProbuf.Protocol.SOCKS)
                    .setType(MessageProbuf.MessageType.INIT)
                    .build();
            ChannelKit.getChannel().attr(ChannelKit.CHANNELS_LOCAL).get().put(requestId, ctx.channel());
            ChannelKit.getChannel().writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()){
                    if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                        ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                    }
                    ctx.pipeline().remove(Socks5InitialAuthHandler.class);
                    ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
                    ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
                    ctx.pipeline().addLast(new Client2DestHandler(channelFuture, requestId));
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }


    private void sendCloseInfo(Channel channel, String requestId){
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRequestId(requestId)
                .setProtocol(MessageProbuf.Protocol.SOCKS)
                .setType(MessageProbuf.MessageType.CLOSE)
                .build();
        channel.writeAndFlush(message);
    }



    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
	private static class Client2DestHandler extends SimpleChannelInboundHandler<Object> {

		private final ChannelFuture destChannelFuture;

        private final String requestId;

		public Client2DestHandler(ChannelFuture destChannelFuture, String requestId) {
			this.destChannelFuture = destChannelFuture;
			this.requestId = requestId;
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
		public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
			log.debug("将客户端的消息转发给目标服务器端");
            final MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
            final ByteBuf buf = (ByteBuf) msg;
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            builder.setBody(ByteString.copyFrom(data));
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setRequestId(requestId)
                    .setRequest(builder.build())
                    .setProtocol(MessageProbuf.Protocol.SOCKS)
                    .setType(MessageProbuf.MessageType.REQUEST)
                    .build();
			destChannelFuture.channel().writeAndFlush(message);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String requestId = ctx.channel().attr(ChannelKit.LOCAL_INFO).get();
            if (StrUtil.isNotBlank(requestId)){
                ChannelKit.getChannel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
            }
			log.debug("客户端断开连接");
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setRequestId(requestId)
                    .setProtocol(MessageProbuf.Protocol.SOCKS)
                    .setType(MessageProbuf.MessageType.CLOSE)
                    .build();
            destChannelFuture.channel().writeAndFlush(message);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (cause instanceof IOException) {
				ctx.close();
			} else {
				super.exceptionCaught(ctx, cause);
			}
		}
	}
}
