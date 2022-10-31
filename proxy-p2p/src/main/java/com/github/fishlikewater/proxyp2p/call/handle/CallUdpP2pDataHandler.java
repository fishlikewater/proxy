package com.github.fishlikewater.proxyp2p.call.handle;

import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月29日 14:44
 **/
@Slf4j
@RequiredArgsConstructor
public class CallUdpP2pDataHandler extends SimpleChannelInboundHandler<ProbufData> {

    private final CallConfig callConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        System.out.println(msgMessage);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final MessageProbuf.Register register = MessageProbuf.Register.newBuilder().setName(callConfig.getName()).build();
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRegister(register)
                .setType(MessageProbuf.MessageType.VALID)
                .build();

        final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                new DefaultAddressedEnvelope<>(message, new InetSocketAddress(callConfig.getServerAddress(), callConfig.getServerPort()), new InetSocketAddress(callConfig.getPort()));
        ctx.writeAndFlush(addressedEnvelope);
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
