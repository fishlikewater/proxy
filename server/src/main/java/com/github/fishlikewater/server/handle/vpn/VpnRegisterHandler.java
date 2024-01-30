package com.github.fishlikewater.server.handle.vpn;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.IpMapping;
import com.github.fishlikewater.server.kit.IpPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd == MessageProtocol.CmdEnum.REGISTER) {
            if (Objects.isNull(msg.getBytes())) {
                final Integer ip = ipPool.getIp();
                String ipStr = proxyConfig.getIpPrefix() + ip;
                mappingIp(ctx, msg, ipStr);
                return;
            }
            //当客户端固定ip时
            final String clientIp = new String(msg.getBytes(), StandardCharsets.UTF_8);
            if (this.determineNotStartWithFixId(clientIp)) {
                final MessageProtocol failMsg = new MessageProtocol();
                failMsg
                        .setId(msg.getId())
                        .setCmd(MessageProtocol.CmdEnum.REGISTER)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setState((byte) 0)
                        .setBytes("ip设置不合规".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(failMsg);
                return;
            }
            final Channel channel = ipMapping.getChannel(clientIp);
            if (Objects.nonNull(channel)) {
                if (this.determineIsActive(channel)) {
                    final MessageProtocol failMsg = new MessageProtocol();
                    failMsg
                            .setId(msg.getId())
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setState((byte) 0)
                            .setBytes(("ip已被使用,请更换 占用连接: " + channel.remoteAddress().toString()).getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(failMsg);
                    return;
                }
                channel.close();
                ipMapping.remove(clientIp);
            }
            mappingIp(ctx, msg, clientIp);
            final int ip = Integer.parseInt(clientIp.replaceAll(proxyConfig.getIpPrefix(), ""));
            ipPool.remove(ip);
            return;
        }
        ctx.fireChannelRead(msg);
    }

    private boolean determineIsActive(Channel channel) {
        return channel.isActive() && channel.isWritable();
    }

    private boolean determineNotStartWithFixId(String clientIp) {
        return !clientIp.startsWith(proxyConfig.getIpPrefix());
    }

    private void mappingIp(ChannelHandlerContext ctx, MessageProtocol msg, String clientIp) {
        ipMapping.put(clientIp, ctx.channel());
        ctx.channel().attr(ChannelGroupKit.VIRT_IP).set(clientIp);
        final MessageProtocol successMsg = new MessageProtocol();
        successMsg
                .setId(msg.getId())
                .setCmd(MessageProtocol.CmdEnum.REGISTER)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setState((byte) 1)
                .setBytes(clientIp.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(successMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.VIRT_IP);
        if (Objects.nonNull(attr)) {
            final String ipStr = attr.get();
            if (CharSequenceUtil.isNotBlank(ipStr)) {
                final int ip = Integer.parseInt(ipStr.replaceAll(proxyConfig.getIpPrefix(), ""));
                ipMapping.remove(ipStr);
                ipPool.retrieve(ip);
            }
        }
        super.channelInactive(ctx);
    }
}
