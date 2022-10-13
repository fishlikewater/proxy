package com.github.fishlikewater.client.handle;

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
public class TcpClientHandler extends SimpleChannelInboundHandler<byte[]> {

    private final String path;

    public TcpClientHandler(String path){
        this.path = path;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
        builder.setBody(ByteString.copyFrom(msg));
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.RESPONSE)
                .setProtocol(MessageProbuf.Protocol.TCP)
                .setRequest(builder.build())
                .setRequestId(path)
                .build();
        ChannelKit.sendMessage(message, t->{
            log.info("发送数据到服务端成功");
        });
    }
}
