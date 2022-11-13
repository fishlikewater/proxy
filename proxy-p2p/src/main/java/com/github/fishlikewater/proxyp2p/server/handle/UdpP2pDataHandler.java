package com.github.fishlikewater.proxyp2p.server.handle;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import com.github.fishlikewater.proxyp2p.kit.ServerKit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.*;

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
public class UdpP2pDataHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        final ByteBuf buf = msg.content();
        final byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        final MessageData messageData = ObjectUtil.deserialize(data);
        final MessageData.CmdEnum type = messageData.getCmdEnum();
        final String name = messageData.getRegisterName();
        if (type == HEALTH || type == VALID){
            ServerKit.ADDRESS_MAP.put(name, msg.sender());
            log.info("get msg {}", type.name());
        }
        if (type == MAKE_HOLE_INIT){
            final InetSocketAddress inetSocketAddress = ServerKit.getADDRESS_MAP().get(name);
            if (inetSocketAddress != null){
                final MessageData.Dst dst1 = new MessageData.Dst()
                        .setDstAddress(inetSocketAddress.getHostName())
                        .setDstPort(inetSocketAddress.getPort());
                final MessageData messageData1 = new MessageData()
                        .setCmdEnum(MAKE_HOLE_INIT)
                        .setDst(dst1);
                final DatagramPacket datagramPacket1 = new DatagramPacket(MessageKit.getByteBuf(messageData1), msg.sender());
                ctx.writeAndFlush(datagramPacket1);

                final MessageData.Dst dst2 = new MessageData.Dst()
                        .setDstAddress(msg.sender().getHostName())
                        .setDstPort(msg.sender().getPort());
                final MessageData messageData2 = new MessageData()
                        .setCmdEnum(MAKE_HOLE_INIT)
                        .setDst(dst2);
                final DatagramPacket datagramPacket2 = new DatagramPacket(MessageKit.getByteBuf(messageData2), inetSocketAddress);
                ctx.writeAndFlush(datagramPacket2);
            }
        }
    }
}
