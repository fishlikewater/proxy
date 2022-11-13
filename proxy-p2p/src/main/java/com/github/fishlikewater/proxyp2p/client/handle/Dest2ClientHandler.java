package com.github.fishlikewater.proxyp2p.client.handle;

import com.github.fishlikewater.proxyp2p.client.ClientKit;
import com.github.fishlikewater.proxyp2p.kit.MessageData;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.CLOSE;
import static com.github.fishlikewater.proxyp2p.kit.MessageData.CmdEnum.RESPONSE;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月18日 22:16
 */
@Slf4j
public class Dest2ClientHandler extends SimpleChannelInboundHandler<Object> {

    private final String requestId;
    private final InetSocketAddress inetSocketAddress;


    public Dest2ClientHandler(String requestId, InetSocketAddress inetSocketAddress) {
        this.requestId = requestId;
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.trace(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object destMsg) {
        log.info("return data");

        final ByteBuf buf = (ByteBuf) destMsg;
        final MessageData messageData = new MessageData()
                .setId(requestId)
                .setCmdEnum(RESPONSE)
                .setByteBuf(buf);
        final ByteBuf byteBuf = MessageKit.getByteBuf(messageData);
        final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, inetSocketAddress);
        ClientKit.channel.writeAndFlush(datagramPacket);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("目标服务器断开连接");
        final MessageData messageData = new MessageData()
                .setId(requestId)
                .setCmdEnum(CLOSE);
        final ByteBuf byteBuf = MessageKit.getByteBuf(messageData);
        final DatagramPacket datagramPacket = new DatagramPacket(byteBuf, inetSocketAddress);
        ClientKit.getChannelMap().remove(requestId);
        ClientKit.channel.writeAndFlush(datagramPacket);
        ctx.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
