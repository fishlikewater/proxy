package com.github.fishlikewater.server.handle.vpn;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.IpMapping;
import com.github.fishlikewater.server.kit.IpPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 类vpn模式注册
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 9:58
 **/
@Slf4j
@RequiredArgsConstructor
public class VpnRegisterHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final IpMapping ipMapping;
    private final IpPool ipPool;
    private final ProxyConfig proxyConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception{
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd == MessageProtocol.CmdEnum.REGISTER) {
            final Integer ip = ipPool.getIp();
            String ipStr = proxyConfig.getIpPrefix() + ip;
            ipMapping.put(ipStr, ctx.channel());
            ctx.channel().attr(ChannelGroupKit.VIRT_IP).set(ipStr);
            final MessageProtocol successMsg = new MessageProtocol();
            successMsg
                    .setId(msg.getId())
                    .setCmd(MessageProtocol.CmdEnum.REGISTER)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setState((byte) 1)
                    .setBytes(ipStr.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(successMsg);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String ipStr = ctx.channel().attr(ChannelGroupKit.VIRT_IP).get();
        final int ip = Integer.parseInt(ipStr.replaceAll(proxyConfig.getIpPrefix(), ""));
        ipPool.retrieve(ip);
        super.channelInactive(ctx);
    }
}
