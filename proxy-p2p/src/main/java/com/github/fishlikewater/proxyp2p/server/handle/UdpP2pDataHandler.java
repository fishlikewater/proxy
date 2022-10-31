package com.github.fishlikewater.proxyp2p.server.handle;

import com.github.fishlikewater.proxyp2p.config.ServerConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import com.github.fishlikewater.proxyp2p.kit.ServerKit;
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

    private final ServerConfig serverConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        final String name = msgMessage.getRegister().getName();
        if (msgMessage.getType() == MessageProbuf.MessageType.HEALTH ||
                msgMessage.getType() == MessageProbuf.MessageType.VALID){
            ServerKit.ADDRESS_MAP.put(name, msg.getSender());
        }
        if (msgMessage.getType() == MessageProbuf.MessageType.MAKE_HOLE_INIT){
            final InetSocketAddress inetSocketAddress = ServerKit.getADDRESS_MAP().get(name);
            if (inetSocketAddress != null){
                final MessageProbuf.Message.Builder builder = MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.MAKE_HOLE_INIT);

                final MessageProbuf.Socks socks1 = MessageProbuf.Socks.newBuilder()
                        .setAddress(inetSocketAddress.getHostString())
                        .setPort(inetSocketAddress.getPort())
                        .build();
                builder.setScoks(socks1);
                final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope1 =
                        new DefaultAddressedEnvelope<>(builder.build(), msg.getSender(), new InetSocketAddress(serverConfig.getPort()));
                ctx.writeAndFlush(addressedEnvelope1);

                final MessageProbuf.Socks socks2 = MessageProbuf.Socks.newBuilder()
                        .setAddress(msg.getSender().getHostString())
                        .setPort(msg.getSender().getPort())
                        .build();
                builder.setScoks(socks2);
                final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope2 =
                        new DefaultAddressedEnvelope<>(builder.build(), inetSocketAddress, new InetSocketAddress(serverConfig.getPort()));
                ctx.writeAndFlush(addressedEnvelope2);

            }
        }
    }


    public static void main(String[] args) {
        final MessageProbuf.Message.Builder builder = MessageProbuf.Message.newBuilder()
                .setType(MessageProbuf.MessageType.MAKE_HOLE_INIT);
        final MessageProbuf.Socks socks1 = MessageProbuf.Socks.newBuilder()
                .setAddress("127.0.0.1")
                .setPort(1000)
                .build();
        final MessageProbuf.Message message1 = builder.setScoks(socks1).build();
        final MessageProbuf.Socks socks2 = MessageProbuf.Socks.newBuilder()
                .setAddress("127.0.0.1")
                .setPort(2000)
                .build();
        final MessageProbuf.Message message2 = builder.setScoks(socks2).build();
        System.out.println(message1);
        System.out.println(message2);
    }
}
