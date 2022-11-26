package com.github.fishlikewater.proxyp2p.client.handle;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxyp2p.client.ClientKit;
import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.CONNECTION;
import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.VALID;

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
public class ClientUdpP2pDataHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final ClientConfig clientConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        final ByteBuf buf = msg.content();
        final byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        final MessageData messageData = ObjectUtil.deserialize(data);
        final MessageData.CmdEnum type = messageData.getCmdEnum();
        final String requestId = messageData.getId();
        Channel channel;
        MessageData.Dst dst;
        System.out.println(type);
        System.out.println(messageData);
        switch (type){
            case MAKE_HOLE_INIT:
                dst = messageData.getDst();
                final DatagramPacket makeHoleMsg = MessageKit.getMakeHoleMsg(dst, new InetSocketAddress(clientConfig.getPort()));
                ctx.writeAndFlush(makeHoleMsg);
                break;
            case MAKE_HOLE:
                ClientKit.setP2pInetSocketAddress(msg.sender());
                log.info("make hole success");
                ctx.writeAndFlush(MessageKit.getAckMsg(msg.sender()));
                break;
            case ACK:
                ClientKit.setP2pInetSocketAddress(msg.sender());
                log.info("confirm message");
                break;
            case CLOSE:
                log.warn("close");
                channel = ClientKit.getChannelMap().get(messageData.getId());
                if(channel != null){
                    ClientKit.getChannelMap().remove(messageData.getId());
                    channel.close();
                }
                break;
            case HEALTH:
                log.debug("health data");
                break;
            case REQUEST:
                channel = ClientKit.getChannelMap().get(messageData.getId());
                if (channel != null && channel.isActive()){
                    ByteBuf byteBuf = messageData.getByteBuf();
                    channel.writeAndFlush(byteBuf).addListener(future -> {
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
                dst = messageData.getDst();
                Bootstrap bootstrap = BootStrapFactroy.bootstrapCenection();
                bootstrap.handler(new NoneClientInitializer());
                bootstrap.remoteAddress(dst.getDstAddress(), dst.getDstPort());
                bootstrap.connect().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ClientKit.getChannelMap().put(requestId, future.channel());
                        future.channel().pipeline().addLast(new Dest2ClientHandler(requestId, msg.sender()));
                        log.debug("连接成功");
                        final MessageData returnData = new MessageData()
                                .setCmdEnum(CONNECTION)
                                .setId(requestId)
                                .setState(1);
                        final ByteBuf byteBuf = MessageKit.getByteBuf(returnData);
                        final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, msg.sender());
                        ctx.writeAndFlush(datagramPacket);
                    } else {
                        log.debug("连接失败");
                        final MessageData returnData = new MessageData()
                                .setCmdEnum(CONNECTION)
                                .setId(requestId)
                                .setState(0);
                        final ByteBuf byteBuf = MessageKit.getByteBuf(returnData);
                        final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, msg.sender());
                        ctx.writeAndFlush(datagramPacket);
                    }
                });
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        final MessageData messageData = new MessageData()
                .setCmdEnum(VALID)
                .setRegisterName(clientConfig.getName());
        final ByteBuf byteBuf = MessageKit.getByteBuf(messageData);
        final DatagramPacket datagramPacket = new DatagramPacket(byteBuf,
                new InetSocketAddress(clientConfig.getServerAddress(), clientConfig.getServerPort()),
                new InetSocketAddress(clientConfig.getPort()));
        ctx.writeAndFlush(datagramPacket);
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
