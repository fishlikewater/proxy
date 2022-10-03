package com.github.fishlikewater.proxy.handler.tcp;

import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description:
 * @date: 2022年07月28日 15:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
public class TcpServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private final String path;

    public TcpServerHandler(String path){
        this.path = path;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
        builder.setBody(ByteString.copyFrom(msg));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.REQUEST)
                .setProtocol(MessageProbuf.Protocol.TCP)
                .setRequest(builder.build())
                .setRequestId(path)
                .build();
        ChannelKit.sendMessage(message, t->{});
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
    }
}
