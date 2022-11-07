package com.github.fishlikewater.proxyp2p.call.handle;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxyp2p.call.CallKit;
import com.github.fishlikewater.proxyp2p.call.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.proxyp2p.call.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.socksx.v5.*;
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
public class CallUdpP2pDataHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final CallConfig callConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        final ByteBuf buf = msg.content();
        final byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        final MessageData messageData = ObjectUtil.deserialize(data);
        final MessageData.CmdEnum type = messageData.getCmdEnum();
        final String requestId = messageData.getId();
        System.out.println(type);
        System.out.println(msg);
        Channel channel;
        switch (type){
            case HEALTH:
                log.info("收到目标心跳消息");
                break;
            case CLOSE:
                channel = CallKit.getChannelMap().get(requestId);
                if (channel != null){
                    CallKit.getChannelMap().remove(requestId);
                    channel.close();
                }
                break;
            case MAKE_HOLE_INIT:
                final MessageData.Dst dst = messageData.getDst();
                ctx.writeAndFlush(MessageKit.getMakeHoleMsg(dst));
                break;
            case MAKE_HOLE:
                CallKit.setP2pInetSocketAddress(msg.sender());
                CallHeartBeatHandler.setInetSocketAddress(msg.sender());
                ctx.writeAndFlush(MessageKit.getAckMsg(msg.sender()));
                log.info("打洞成功");
                break;
            case ACK:
                CallKit.setP2pInetSocketAddress(msg.sender());
                CallHeartBeatHandler.setInetSocketAddress(msg.sender());
                log.info("confirm message");
                break;
            case CONNECTION:
                Socks5CommandResponse commandResponse;
                if (messageData.getState() == 1) {
                    commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                }else {
                    commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                }
                channel = CallKit.getChannelMap().get(messageData.getId());
                if (channel != null) {
                    if (channel.pipeline().get(Socks5CommandRequestHandler.class) != null) {
                        channel.pipeline().remove(Socks5CommandRequestHandler.class);
                        channel.pipeline().remove(Socks5CommandRequestDecoder.class);
                        channel.pipeline().remove(Socks5InitialAuthHandler.class);
                        channel.pipeline().remove(Socks5InitialRequestDecoder.class);
                    }
                    channel.pipeline().addLast(new Socks5CommandRequestHandler.Client2DestHandler(messageData.getId(), callConfig));
                    channel.writeAndFlush(commandResponse);
                }else {
                    sendCloseInfo(messageData.getId(), msg.sender(), ctx);
                }
                break;
            case RESPONSE:
                final String id = messageData.getId();
                channel = CallKit.getChannelMap().get(id);
                if (channel != null && channel.isActive()){
                    final ByteBuf byteBuf = messageData.getByteBuf();
                    channel.writeAndFlush(byteBuf);
                }

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

    private void sendCloseInfo(String requestId, InetSocketAddress inetSocketAddress, ChannelHandlerContext ctx){
        final MessageProbuf.Message message = MessageProbuf.Message.newBuilder()
                .setId(requestId)
                .setType(MessageProbuf.MessageType.CLOSE)
                .build();
        final AddressedEnvelope<MessageProbuf.Message, InetSocketAddress> addressedEnvelope =
                new DefaultAddressedEnvelope<>(message, inetSocketAddress, new InetSocketAddress(callConfig.getPort()));
        ctx.writeAndFlush(addressedEnvelope);
    }
}
