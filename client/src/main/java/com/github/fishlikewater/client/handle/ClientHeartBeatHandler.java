package com.github.fishlikewater.client.handle;


import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.kit.IdUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 * @author fishlikewater@126.com
 * @since 2022年11月19日 13:03
 */
@Slf4j
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    public static final MessageProtocol HEARTBEAT_SEQUENCE = new MessageProtocol()
            .setId(IdUtil.id())
            .setCmd(MessageProtocol.CmdEnum.HEALTH)
            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            //IdleStateEvent event = (IdleStateEvent) evt;        // 强制类型转换
            ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE)
                    .addListener((future)->{
                        if(!future.isSuccess()){
                            log.warn("发送心跳包失败...");
                        }

                    });//(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
