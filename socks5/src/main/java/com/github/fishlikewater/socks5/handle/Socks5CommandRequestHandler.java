package com.github.fishlikewater.socks5.handle;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.kit.IdUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author fishl
 */
@Slf4j
@RequiredArgsConstructor
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private final Channel channel;

    private final boolean checkConnect;


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) {
        log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            final Long requestId = IdUtil.id();
            ctx.channel().attr(Socks5Kit.LOCAL_INFO).set(requestId);
            final MessageProtocol.Dst dst = new MessageProtocol.Dst();
            dst.setDstAddress(msg.dstAddr());
            dst.setDstPort(msg.dstPort());
            channel.attr(Socks5Kit.CHANNELS_SOCKS).get().put(requestId, ctx.channel());
            final ChannelPipeline pipeline = ctx.channel().pipeline();
            if (checkConnect){
                MessageProtocol message = new MessageProtocol();
                message
                        .setCmd(MessageProtocol.CmdEnum.CONNECTION)
                        .setId(requestId)
                        .setDst(dst)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
                channel.writeAndFlush(message).addListener(future -> {
                    if (future.isSuccess()) {
                        if (ctx.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                            ctx.pipeline().remove(Socks5CommandRequestHandler.class);
                        }
                        pipeline.remove(Socks5InitialAuthHandler.class);
                        pipeline.remove(Socks5InitialRequestDecoder.class);
                        pipeline.remove(Socks5CommandRequestDecoder.class);
                        pipeline.addLast(new Client2DestHandler(channel, requestId, dst));
                        if (ctx.pipeline().get(Socks5PasswordAuthRequestHandler.class) != null) {
                            pipeline.remove(Socks5PasswordAuthRequestHandler.class);
                        }
                        if (pipeline.get(Socks5PasswordAuthRequestDecoder.class) != null) {
                            pipeline.remove(Socks5PasswordAuthRequestDecoder.class);
                        }
                    }else {
                        log.info("无法连接目标");
                    }
                });
            }else {
                Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                pipeline.remove(Socks5InitialAuthHandler.class);
                pipeline.remove(Socks5InitialRequestDecoder.class);
                pipeline.remove(Socks5CommandRequestDecoder.class);
                pipeline.addLast(new Client2DestHandler(channel, requestId, dst));
                if (ctx.pipeline().get(Socks5PasswordAuthRequestHandler.class) != null) {
                    ctx.pipeline().remove(Socks5PasswordAuthRequestHandler.class);
                }
                if (ctx.pipeline().get(Socks5PasswordAuthRequestDecoder.class) != null) {
                    ctx.pipeline().remove(Socks5PasswordAuthRequestDecoder.class);
                }
                if (pipeline.get(Socks5CommandRequestHandler.class) != null) {
                    pipeline.remove(Socks5CommandRequestHandler.class);
                }
                ctx.channel().writeAndFlush(commandResponse);
            }

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
    private static class Client2DestHandler extends SimpleChannelInboundHandler<Object> {

        private final Channel channel;

        private final Long requestId;

        private final MessageProtocol.Dst dst;

        public Client2DestHandler(Channel channel, Long requestId, MessageProtocol.Dst dst) {
            this.channel = channel;
            this.requestId = requestId;
            this.dst = dst;
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            boolean canWrite = ctx.channel().isWritable();
            log.trace(ctx.channel() + " 可写性：" + canWrite);
            //流量控制，不允许继续读
            channel.config().setAutoRead(canWrite);
            super.channelWritabilityChanged(ctx);
        }


        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) {
            log.debug("将客户端的消息转发给目标服务器端");
            final ByteBuf buf = (ByteBuf) msg;
            final MessageProtocol message = new MessageProtocol();
            message
                    .setId(requestId)
                    .setDst(dst)
                    .setCmd(MessageProtocol.CmdEnum.REQUEST)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setBytes(ByteBufUtil.getBytes(buf));
            channel.writeAndFlush(message);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            final Long requestId = ctx.channel().attr(Socks5Kit.LOCAL_INFO).get();
            if (ObjectUtil.isNotNull(requestId)) {
                channel.attr(Socks5Kit.CHANNELS_SOCKS).get().remove(requestId);
            }
            log.debug("客户端断开连接");
            final MessageProtocol message = new MessageProtocol();
            message
                    .setId(requestId)
                    .setCmd(MessageProtocol.CmdEnum.CLOSE)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
            channel.writeAndFlush(message);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
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
