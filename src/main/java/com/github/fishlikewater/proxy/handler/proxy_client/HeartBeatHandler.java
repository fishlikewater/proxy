package com.github.fishlikewater.proxy.handler.proxy_client;


import com.github.fishlikewater.proxy.boot.ConnectionListener;
import com.github.fishlikewater.proxy.boot.NettyProxyClient;
import com.github.fishlikewater.proxy.kit.IdUtil;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private NettyProxyClient client;

    public HeartBeatHandler(NettyProxyClient client){
        this.client = client;
    }

    public static final MessageProbuf.Message HEARTBEAT_SEQUENCE = MessageProbuf.Message.newBuilder()
            .setLength(10)
            .setRequestId(IdUtil.next())
            .setExtend("ping")
            .setType(MessageProbuf.MessageType.HEALTH)
            .build();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;        // 强制类型转换
            ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE)
                    .addListener(new ConnectionListener(client));//(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
