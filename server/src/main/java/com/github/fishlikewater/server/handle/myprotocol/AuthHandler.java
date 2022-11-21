package com.github.fishlikewater.server.handle.myprotocol;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.ConnectionValidate;
import io.netty.channel.*;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月19日 14:25
 **/
@RequiredArgsConstructor
public class AuthHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ConnectionValidate connectionValidate;
    private final ProxyConfig proxyConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {

        if (msg.getCmd() == MessageProtocol.CmdEnum.DATA_CHANNEL){
            //处理 建立数据通道的请求
            //1、先验证主连接
            final String mainChannelId = new String(msg.getBytes(), StandardCharsets.UTF_8);
            final Channel channel = ChannelGroupKit.find(mainChannelId);
            if (Objects.nonNull(channel) && channel.isActive()){

            }
        }

        if (msg.getCmd() == MessageProtocol.CmdEnum.AUTH){
            //处理 连接安全验证
            final String token = new String(msg.getBytes(), StandardCharsets.UTF_8);
            boolean validate = connectionValidate.validate(token, proxyConfig.getToken());
            if (validate)
            {
                ctx.pipeline().remove(this);
                final MessageProtocol successMsg = new MessageProtocol();
                successMsg
                        .setCmd(MessageProtocol.CmdEnum.AUTH)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setId(msg.getId())
                        .setState((byte) 1);
                ctx.writeAndFlush(successMsg);
            }else
            {
                final MessageProtocol failMsg = new MessageProtocol();
                failMsg
                        .setCmd(MessageProtocol.CmdEnum.AUTH)
                        .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                        .setId(msg.getId())
                        .setState((byte) 0)
                        .setBytes("验证失败".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(failMsg);
            }
        }else {
            final MessageProtocol failMsg = new MessageProtocol();
            failMsg
                    .setCmd(MessageProtocol.CmdEnum.AUTH)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setId(msg.getId())
                    .setState((byte) 0)
                    .setBytes("请先验证token".getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(failMsg);
        }
    }
}
