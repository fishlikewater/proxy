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
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) throws Exception {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        System.out.println(msgMessage);
        final MessageProbuf.MessageType type = msgMessage.getType();
        if (type == MessageProbuf.MessageType.MAKE_HOLE_INIT){
            final MessageProbuf.Socks scoks = msgMessage.getScoks();
            final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                    .setType(MessageProbuf.MessageType.MAKE_HOLE)
                    .build();
            final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                    new DefaultAddressedEnvelope<>(message, new InetSocketAddress(scoks.getAddress(), scoks.getPort()),
                            new InetSocketAddress(callConfig.getPort()));
            ctx.writeAndFlush(addressedEnvelope);
            Thread.sleep(5000);
            ctx.writeAndFlush(addressedEnvelope);
        }
        if (type == MessageProbuf.MessageType.MAKE_HOLE){
            log.info("打洞成功");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final MessageProbuf.Register register = MessageProbuf.Register.newBuilder().setName(callConfig.getName()).build();
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRegister(register)
                .setType(MessageProbuf.MessageType.MAKE_HOLE_INIT)
                .build();

        final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                new DefaultAddressedEnvelope<>(message, new InetSocketAddress(callConfig.getServerAddress(), callConfig.getServerPort()),
                        new InetSocketAddress(callConfig.getPort()));
        ctx.writeAndFlush(addressedEnvelope);
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
