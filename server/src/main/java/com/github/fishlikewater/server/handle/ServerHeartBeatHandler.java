package com.github.fishlikewater.server.handle;

import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class ServerHeartBeatHandler extends ChannelInboundHandlerAdapter {
    public static final MessageProbuf.Message HEARTBEAT_SEQUENCE = MessageProbuf.Message.newBuilder()
            .setLength(10)
            .setExtend("ping")
            .setType(MessageProbuf.MessageType.HEALTH)
            .build();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("服务端主动关闭连接");
                ctx.close();
           /*     ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE)
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if (!channelFuture.isSuccess()) {
                                    log.info("关闭连接");
                                    ctx.channel().close();
                                }
                            }
                        });//(ChannelFutureListener.CLOSE_ON_FAILURE);*/
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
