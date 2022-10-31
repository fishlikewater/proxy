package com.github.fishlikewater.proxyp2p.client.handle;


import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final ClientConfig clientConfig;

    public static MessageProbuf.Register register;

    public static MessageProbuf.Message HEARTBEAT_SEQUENCE;

    public ClientHeartBeatHandler(ClientConfig clientConfig){
        this.clientConfig = clientConfig;
        register = MessageProbuf.Register.newBuilder().setName(clientConfig.getName()).build();
        HEARTBEAT_SEQUENCE = MessageProbuf.Message.newBuilder()
                .setRegister(register)
                .setType(MessageProbuf.MessageType.HEALTH)
                .build();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(HEARTBEAT_SEQUENCE, new InetSocketAddress(clientConfig.getServerAddress(), clientConfig.getServerPort()), new InetSocketAddress(clientConfig.getPort()));

            ctx.writeAndFlush(addressedEnvelope)
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
