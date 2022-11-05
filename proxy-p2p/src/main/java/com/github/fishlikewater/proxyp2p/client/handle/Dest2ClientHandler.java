package com.github.fishlikewater.proxyp2p.client.handle;
import com.github.fishlikewater.proxyp2p.client.ClientKit;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    private final String requestId;
    private final InetSocketAddress inetSocketAddress;


    public Dest2ClientHandler(String requestId, InetSocketAddress inetSocketAddress) {
        this.requestId = requestId;
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.trace(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] destMsg) {
        log.info("return data");
        final MessageProbuf.Response.Builder builder = MessageProbuf.Response.newBuilder();
        builder.setResponseBody(ByteString.copyFrom(destMsg));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setId(requestId)
                .setResponse(builder.build())
                .setType(MessageProbuf.MessageType.RESPONSE)
                .build();
        final DefaultAddressedEnvelope<MessageProbuf.Message, InetSocketAddress> msg = new DefaultAddressedEnvelope<>(message, inetSocketAddress,
                new InetSocketAddress(0));
        ClientKit.channel.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.trace("目标服务器断开连接");
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setId(requestId)
                .setType(MessageProbuf.MessageType.CLOSE)
                .build();
        final DefaultAddressedEnvelope<MessageProbuf.Message, InetSocketAddress> msg = new DefaultAddressedEnvelope<>(message, inetSocketAddress,
                new InetSocketAddress(0));
        ClientKit.getChannelMap().remove(requestId);
        ClientKit.channel.writeAndFlush(msg);
        ctx.close();

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
