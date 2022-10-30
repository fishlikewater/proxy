package com.github.fishlikewater.proxyp2p.client.handle;

import com.github.fishlikewater.proxyp2p.config.ClientConfig;
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
public class UdpP2pDataHandler extends SimpleChannelInboundHandler<ProbufData> {

    private final ClientConfig clientConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) throws Exception {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        System.out.println(msgMessage);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端向服务器发送自己的IP和PORT");
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setLength(100)
                .setType(MessageProbuf.MessageType.MAKE_HOLE_INIT)
                .build();
        final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                new DefaultAddressedEnvelope<>(message, new InetSocketAddress(clientConfig.getServerAddress(), clientConfig.getServerPort()), new InetSocketAddress(clientConfig.getPort()));
        ctx.writeAndFlush(addressedEnvelope);
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
