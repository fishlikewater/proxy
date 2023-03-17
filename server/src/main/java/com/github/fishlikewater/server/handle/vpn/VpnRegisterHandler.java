package com.github.fishlikewater.server.handle.vpn;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.HandleKit;
import com.github.fishlikewater.server.kit.IpMapping;
import com.github.fishlikewater.server.kit.IpPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 *  类vpn模式注册
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
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd ==  MessageProtocol.CmdEnum.REGISTER){
            final byte[] bytes = msg.getBytes();
            final String registerName = new String(bytes, StandardCharsets.UTF_8);
            // 未提供注册名(注册名对于受控制机全局唯一，为方便使用端 采用自定义设置)
            final boolean checkRegisterName = HandleKit.checkRegisterName(ctx, registerName, msg.getId());
            if (!checkRegisterName){
                return;
            }
            final boolean b = HandleKit.checkRegisterNameIsUse(registerName, msg.getId(), ctx);
            if (b){
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
                log.info("register client path {} successful", registerName);
            }
        }
    }
}
