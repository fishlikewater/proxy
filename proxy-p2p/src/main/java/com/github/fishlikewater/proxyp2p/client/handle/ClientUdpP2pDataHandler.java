package com.github.fishlikewater.proxyp2p.client.handle;

import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.proxyp2p.client.ClientKit;
import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedWriteHandler;
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
public class ClientUdpP2pDataHandler extends SimpleChannelInboundHandler<ProbufData> {

    private final ClientConfig clientConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProbufData msg) {
        final MessageProbuf.Message msgMessage = (MessageProbuf.Message)msg.getMessage();
        final MessageProbuf.MessageType type = msgMessage.getType();
        if (type == MessageProbuf.MessageType.MAKE_HOLE_INIT){
            final MessageProbuf.Socks scoks = msgMessage.getScoks();
            ctx.writeAndFlush(MessageKit.getMakeHoleMsg(scoks));
        }
        if (type == MessageProbuf.MessageType.MAKE_HOLE){
            ClientKit.setP2pInetSocketAddress(msg.getSender());
            log.info("make hole success");
        }
        if (type == MessageProbuf.MessageType.CLOSE){
            log.warn("close");
            final Channel channel = ClientKit.getChannelMap().get(msgMessage.getId());
            if(channel != null){
                channel.close();
            }
        }
        if (type == MessageProbuf.MessageType.HEALTH){
            log.info("health data");
        }
        if (type == MessageProbuf.MessageType.CONNECTION){
           MessageProbuf.Socks scoks = msgMessage.getScoks();
            final String requestId = msgMessage.getId();
            Bootstrap bootstrap = BootStrapFactroy.bootstrapCenection();
            bootstrap.handler(new NoneClientInitializer());
            bootstrap.remoteAddress(scoks.getAddress(), scoks.getPort());
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ClientKit.getChannelMap().put(requestId, future.channel());
                    future.channel().pipeline().addLast(new ByteArrayCodec());
                    future.channel().pipeline().addLast(new ChunkedWriteHandler());
                    future.channel().pipeline().addLast(new Dest2ClientHandler(requestId, msg.getSender()));
                    log.debug("连接成功");
                    MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                            .setId(requestId)
                            .setType(MessageProbuf.MessageType.CONNECTION)
                            .setLength(1)
                            .build();
                    final DefaultAddressedEnvelope<MessageProbuf.Message, InetSocketAddress> success =
                            new DefaultAddressedEnvelope<>(message, msg.getSender(),
                            new InetSocketAddress(clientConfig.getPort()));
                    ctx.writeAndFlush(success);
                } else {
                    log.debug("连接失败");
                    MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                            .setId(requestId)
                            .setType(MessageProbuf.MessageType.CONNECTION)
                            .setLength(0)
                            .build();
                    final DefaultAddressedEnvelope<MessageProbuf.Message, InetSocketAddress> fail =
                            new DefaultAddressedEnvelope<>(message, msg.getSender(),
                                    new InetSocketAddress(clientConfig.getPort()));
                    ctx.writeAndFlush(fail);
                }
            });
        }
        if (type == MessageProbuf.MessageType.REQUEST){
            final Channel channel = ClientKit.getChannelMap().get(msgMessage.getId());
            if (channel != null && channel.isActive()){
                final byte[] bytes = msgMessage.getRequest().getRequestBody().toByteArray();
                channel.writeAndFlush(bytes);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final MessageProbuf.Register register = MessageProbuf.Register.newBuilder().setName(clientConfig.getName()).build();
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setRegister(register)
                .setType(MessageProbuf.MessageType.VALID)
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
