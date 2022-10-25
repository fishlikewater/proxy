package com.github.fishlikewater.client.handle;
import com.github.fishlikewater.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月18日 22:16
 */
@Slf4j
public class Dest2ClientHandler extends SimpleChannelInboundHandler<byte[]> {

    private final ChannelHandlerContext clientChannelContext;
    private final String requestId;
    private final String callId;

    public Dest2ClientHandler(ChannelHandlerContext clientChannelContext, String requestId, String callId) {
        this.clientChannelContext = clientChannelContext;
        this.requestId = requestId;
        this.callId = callId;
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
    public void channelRead0(ChannelHandlerContext ctx, byte[] destMsg) {
        final MessageProbuf.Response.Builder builder = MessageProbuf.Response.newBuilder();
        builder.setBody(ByteString.copyFrom(destMsg));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRequestId(requestId)
                .setClientId(callId)
                .setResponse(builder.build())
                .setProtocol(MessageProbuf.Protocol.SOCKS)
                .setType(MessageProbuf.MessageType.RESPONSE)
                .build();
        clientChannelContext.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.trace("目标服务器断开连接");
        ctx.close();
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRequestId(requestId)
                .setProtocol(MessageProbuf.Protocol.SOCKS)
                .setType(MessageProbuf.MessageType.CLOSE)
                .build();
        clientChannelContext.writeAndFlush(message);
        clientChannelContext.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);

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
