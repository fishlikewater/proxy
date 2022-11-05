package com.github.fishlikewater.proxyp2p.call.handle.socks;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.proxyp2p.call.CallKit;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
@RequiredArgsConstructor
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private final CallConfig callConfig;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final String requestId = ctx.channel().attr(CallKit.LOCAL_INFO).get();
        if (StrUtil.isNotBlank(requestId)){
            CallKit.getChannelMap().remove(requestId);
        }
        //sendCloseInfo(requestId);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
       super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
		log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            final String requestId = IdUtil.next();
            ctx.channel().attr(CallKit.LOCAL_INFO).set(requestId);
            final MessageProbuf.Socks.Builder socksBuilder = MessageProbuf.Socks.newBuilder();
            socksBuilder.setAddress(msg.dstAddr());
            socksBuilder.setPort(msg.dstPort());
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setScoks(socksBuilder.build())
                    .setId(requestId)
                    .setType(MessageProbuf.MessageType.CONNECTION)
                    .build();
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(message, CallKit.p2pInetSocketAddress,
                            new InetSocketAddress(callConfig.getPort()));
            CallKit.getChannelMap().put(requestId, ctx.channel());
            CallKit.channel.writeAndFlush(addressedEnvelope).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()){
                    if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                        ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                    }
                    ctx.pipeline().addLast(new Client2DestHandler(requestId, callConfig));
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


    private void sendCloseInfo(String requestId){
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setId(requestId)
                .setType(MessageProbuf.MessageType.CLOSE)
                .build();
        final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                new DefaultAddressedEnvelope<>(message, CallKit.p2pInetSocketAddress,
                        new InetSocketAddress(callConfig.getPort()));
        CallKit.channel.writeAndFlush(addressedEnvelope);
    }



    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
	private static class Client2DestHandler extends SimpleChannelInboundHandler<Object> {

        private final String requestId;

        private final CallConfig callConfig;

		public Client2DestHandler(String requestId, CallConfig callConfig) {
			this.requestId = requestId;
			this.callConfig = callConfig;
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx, Object msg) {
			log.debug("将客户端的消息转发给目标服务器端");
            final MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
            final ByteBuf buf = (ByteBuf) msg;
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            builder.setRequestBody(ByteString.copyFrom(data));
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setId(requestId)
                    .setRequest(builder.build())
                    .setType(MessageProbuf.MessageType.REQUEST)
                    .build();
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(message, CallKit.p2pInetSocketAddress,
                            new InetSocketAddress(callConfig.getPort()));
            CallKit.channel.writeAndFlush(addressedEnvelope);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
            final String requestId = ctx.channel().attr(CallKit.LOCAL_INFO).get();
            if (StrUtil.isNotBlank(requestId)){
                CallKit.getChannelMap().remove(requestId);
            }
			log.debug("客户端断开连接");
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setId(requestId)
                    .setType(MessageProbuf.MessageType.CLOSE)
                    .build();
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(message, CallKit.p2pInetSocketAddress,
                            new InetSocketAddress(callConfig.getPort()));
           CallKit.channel.writeAndFlush(addressedEnvelope);
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
