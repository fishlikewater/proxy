package com.github.fishlikewater.server.handle.vpn;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.IpMapping;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 *  vpn 模式消息处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 14:35
 **/
@Slf4j
@RequiredArgsConstructor
public class VpnMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final IpMapping ipMapping;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd != MessageProtocol.CmdEnum.CLOSE && cmd != MessageProtocol.CmdEnum.HEALTH){
            final MessageProtocol.Dst dst = msg.getDst();
            final String host = dst.getDstAddress();
            final Channel channel = ipMapping.getChannel(host);
            if (Objects.nonNull(channel) && channel.isActive()){
                final String virIp = ctx.channel().attr(ChannelGroupKit.VIRT_IP).get();
                dst.setDstAddress(virIp);
                channel.writeAndFlush(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof TooLongFrameException){
            final Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.VIRT_IP);
            log.error("报错的连接: {}", attr.get() == null?ctx.channel().remoteAddress().toString():attr.get());
        }
        super.exceptionCaught(ctx, cause);
    }
}
