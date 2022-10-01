package com.github.fishlikewater.proxy.handler.tcp;

import com.github.fishlikewater.proxy.boot.TcpProxyClient;
import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.handler.proxy_server.CacheUtil;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @date: 2022年07月28日 15:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
public class TcpHandler extends SimpleChannelInboundHandler<byte[]> {

    private final String path;

    public TcpHandler(String path){
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
}
