package com.github.fishlikewater.server.handle.http;

import com.github.fishlikewater.codec.HttpProtocol;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ConnectionValidate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 *  验证器
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月19日 14:25
 **/
@RequiredArgsConstructor
public class HttpAuthHandler extends SimpleChannelInboundHandler<HttpProtocol> {

    private final ConnectionValidate connectionValidate;
    private final ProxyConfig proxyConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpProtocol msg) {

        if (msg.getCmd() == HttpProtocol.CmdEnum.AUTH) {
            //处理 连接安全验证
            final String token = new String(msg.getBytes(), StandardCharsets.UTF_8);
            boolean validate = connectionValidate.validate(token, proxyConfig.getToken());
            if (validate) {
                ctx.pipeline().remove(this);
                final HttpProtocol successMsg = new HttpProtocol();
                successMsg
                        .setCmd(HttpProtocol.CmdEnum.AUTH)
                        .setId(msg.getId())
                        .setBytes("验证成功".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(successMsg);
            } else {
                final HttpProtocol failMsg = new HttpProtocol();
                failMsg
                        .setCmd(HttpProtocol.CmdEnum.AUTH)
                        .setId(msg.getId())
                        .setBytes("验证失败".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(failMsg);
            }
        } else {
            final HttpProtocol failMsg = new HttpProtocol();
            failMsg
                    .setCmd(HttpProtocol.CmdEnum.AUTH)
                    .setId(msg.getId())
                    .setBytes("请先验证token".getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(failMsg);
        }
    }
}
