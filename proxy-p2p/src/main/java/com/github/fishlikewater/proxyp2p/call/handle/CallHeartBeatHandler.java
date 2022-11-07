package com.github.fishlikewater.proxyp2p.call.handle;


import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxyp2p.call.CallKit;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.HEALTH;

/**
 * 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class CallHeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Setter
    public static InetSocketAddress inetSocketAddress;

    public static ByteBuf HEARTBEAT_SEQUENCE;

    public CallHeartBeatHandler(CallConfig callConfig){
        MessageData messageData = new MessageData()
                .setRegisterName(callConfig.getName())
                .setCmdEnum(HEALTH);
        final byte[] bytesMsg = ObjectUtil.serialize(messageData);
        HEARTBEAT_SEQUENCE = ByteBufAllocator.DEFAULT.buffer(bytesMsg.length);
        HEARTBEAT_SEQUENCE.writeBytes(bytesMsg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            final DatagramPacket datagramPacket = new DatagramPacket(HEARTBEAT_SEQUENCE, CallKit.p2pInetSocketAddress);
            ctx.writeAndFlush(datagramPacket)
                    .addListener((future)->{
                        if(!future.isSuccess()){
                            log.warn("发送心跳包失败...");
                        }
                    });
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
