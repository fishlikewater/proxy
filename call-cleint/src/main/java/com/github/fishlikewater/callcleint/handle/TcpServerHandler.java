package com.github.fishlikewater.callcleint.handle;

import com.github.fishlikewater.callcleint.config.ProxyConfig;
import com.github.fishlikewater.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
    private final ProxyConfig.Mapping mapping;

    public TcpServerHandler(String path, ProxyConfig.Mapping mapping){
        this.path = path;
        this.mapping = mapping;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {
        MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
        builder.setBody(ByteString.copyFrom(msg));
        builder.putHeader("address", mapping.getRemoteAddress());
        builder.putHeader("port", String.valueOf(mapping.getRemotePort()));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.REQUEST)
                .setProtocol(MessageProbuf.Protocol.TCP)
                .setRequest(builder.build())
                .setRequestId(ChannelKit.getRequestId())
                .setExtend(path)
                .build();
        ChannelKit.sendMessage(message, t->{});
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelKit.getChannel().attr(ChannelKit.CHANNELS).set(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //Optional.ofNullable(ChannelKit.getChannel().attr(ChannelKit.CHANNELS).get()).ifPresent(channelMap -> channelMap.remove(ctx.channel().id().asLongText()));
        close();
        super.channelInactive(ctx);
    }

    /**
     * 连接异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        close();

    }


    private void  close(){
        ChannelKit.getChannel().attr(ChannelKit.CHANNELS).set(null);
        MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
        builder.putHeader("address", mapping.getRemoteAddress());
        builder.putHeader("port", String.valueOf(mapping.getRemotePort()));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.CLOSE)
                .setProtocol(MessageProbuf.Protocol.TCP)
                .setRequest(builder.build())
                .setExtend(path)
                .setRequestId(ChannelKit.getRequestId())
                .build();
        ChannelKit.sendMessage(message, t->{});
    }
}
