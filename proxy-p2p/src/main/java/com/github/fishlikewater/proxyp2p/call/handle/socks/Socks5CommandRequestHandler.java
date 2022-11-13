package com.github.fishlikewater.proxyp2p.call.handle.socks;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.proxyp2p.call.CallKit;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.CLOSE;

@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

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
            final MessageData.Dst dst = new MessageData.Dst();
            dst.setDstAddress(msg.dstAddr());
            dst.setDstPort(msg.dstPort());
            final MessageData messageData = new MessageData();
            messageData.setDst(dst);
            messageData.setId(requestId);
            messageData.setCmdEnum(MessageData.CmdEnum.CONNECTION);
            final byte[] bytes = ObjectUtil.serialize(messageData);
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buf.writeBytes(bytes);
            final DatagramPacket datagramPacket = new DatagramPacket(buf, CallKit.p2pInetSocketAddress);
            CallKit.getChannelMap().put(requestId, ctx.channel());
            CallKit.channel.writeAndFlush(datagramPacket).addListener((ChannelFutureListener) channelFuture -> {
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



    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
	public static class Client2DestHandler extends SimpleChannelInboundHandler<Object> {

        private final String requestId;

        public Client2DestHandler(String requestId) {
			this.requestId = requestId;
        }

		@Override
		public void channelRead0(ChannelHandlerContext ctx, Object msg) {
			log.debug("将客户端的消息转发给目标服务器端");
            final ByteBuf buf = (ByteBuf) msg;
            final ByteBuf byteBuf = MessageKit.getByteBuf(buf, MessageData.CmdEnum.REQUEST, requestId);
            final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, CallKit.p2pInetSocketAddress);
            CallKit.channel.writeAndFlush(datagramPacket);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
            final String requestId = ctx.channel().attr(CallKit.LOCAL_INFO).get();
            if (StrUtil.isNotBlank(requestId)){
                CallKit.getChannelMap().remove(requestId);
            }
			log.debug("客户端断开连接");
            final MessageData messageData = new MessageData()
                    .setId(requestId)
                    .setCmdEnum(CLOSE);
            final ByteBuf byteBuf = MessageKit.getByteBuf(messageData);
            final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, CallKit.p2pInetSocketAddress);
           CallKit.channel.writeAndFlush(datagramPacket);
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
