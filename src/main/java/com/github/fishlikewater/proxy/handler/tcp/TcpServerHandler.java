package com.github.fishlikewater.proxy.handler.tcp;

import com.github.fishlikewater.proxy.handler.proxy_client.ChannelKit;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @Description:
 * @date: 2022年07月28日 15:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
public class TcpServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private final String path;

    public TcpServerHandler(String path){
        this.path = path;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("接受tcp服务端请求");
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
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelKit.getChannel().attr(ChannelKit.CHANNELS).set(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //Optional.ofNullable(ChannelKit.getChannel().attr(ChannelKit.CHANNELS).get()).ifPresent(channelMap -> channelMap.remove(ctx.channel().id().asLongText()));
        ChannelKit.getChannel().attr(ChannelKit.CHANNELS).set(null);
        super.channelInactive(ctx);
    }

    /**
     * 连接异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
