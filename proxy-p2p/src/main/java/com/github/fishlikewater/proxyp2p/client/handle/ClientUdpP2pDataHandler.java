package com.github.fishlikewater.proxyp2p.client.handle;

import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.proxyp2p.client.ClientKit;
import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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
        Channel channel;
        MessageProbuf.Socks scoks;
        System.out.println(type);
        System.out.println(msg);
        switch (type){
            case MAKE_HOLE_INIT:
                scoks = msgMessage.getScoks();
                ctx.writeAndFlush(MessageKit.getMakeHoleMsg(scoks));
                break;
            case MAKE_HOLE:
                ClientKit.setP2pInetSocketAddress(msg.getSender());
                log.info("make hole success");
                ctx.writeAndFlush(MessageKit.getAckMsg(msg.getSender()));
                break;
            case ACK:
                ClientKit.setP2pInetSocketAddress(msg.getSender());
                log.info("confirm message");
                break;
            case CLOSE:
                log.warn("close");
                channel = ClientKit.getChannelMap().get(msgMessage.getId());
                if(channel != null){
                    ClientKit.getChannelMap().remove(msgMessage.getId());
                    channel.close();
                }
                break;
            case HEALTH:
                log.debug("health data");
                break;
            case REQUEST:
                channel = ClientKit.getChannelMap().get(msgMessage.getId());
                if (channel != null && channel.isActive()){
                    final byte[] bytes = msgMessage.getRequest().getRequestBody().toByteArray();
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
                    buf.writeBytes(bytes);
                    channel.writeAndFlush(buf).addListener(future -> {
                        if (future.isSuccess()){
                            log.info("send success");
                        }else {
                            log.warn("send fail");
                        }
                    });
                }else {
                    log.warn("connect not found");
                }
                break;
            case CONNECTION:
                scoks = msgMessage.getScoks();
                final String requestId = msgMessage.getId();
                Bootstrap bootstrap = BootStrapFactroy.bootstrapCenection();
                bootstrap.handler(new NoneClientInitializer());
                bootstrap.remoteAddress(scoks.getAddress(), scoks.getPort());
                bootstrap.connect().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ClientKit.getChannelMap().put(requestId, future.channel());
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
