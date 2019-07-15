package com.github.fishlikewater.proxy.handler.proxy_client;

import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 13:57
 * @since
 **/
public class ToServerHandler extends SimpleChannelInboundHandler {

    private Channel outChannel;

    private String requestId;

    public ToServerHandler(Channel outChannel, String requestId) {
        this.outChannel = outChannel;
        this.requestId = requestId;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("交换数据");
        if(msg instanceof FullHttpResponse){
            FullHttpResponse resp = (FullHttpResponse) msg;
            MessageProbuf.Response.Builder builder = MessageProbuf.Response.newBuilder();
            int code = resp.status().code();
            Map<String, String> header = new HashMap<>();
            resp.headers().entries().forEach(t->{
                header.put(t.getKey(), t.getValue());
            });
            ByteBuf content = resp.content();
            if(content.hasArray()){
                builder.setBody(ByteString.copyFrom(content.array()));
            }else {
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                builder.setBody(ByteString.copyFrom(bytes));
            }
            builder.setCode(code);
            builder.putAllHeader(header);
            outChannel.write(MessageProbuf.Message.newBuilder()
                    .setRequestId(requestId)
                    .setResponse(builder.build())
                    .setType(MessageProbuf.MessageType.RESPONSE)).addListener(t->{
            });
            builder = null;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        outChannel.flush();
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
