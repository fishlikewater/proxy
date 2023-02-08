package com.github.fishlikewater.server.handle.http;

import com.github.fishlikewater.codec.HttpProtocol;
import com.github.fishlikewater.server.kit.CacheUtil;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 *    处理客户端返回的数据
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2023年02月07日 11:39
 **/
@Slf4j
public class HttpProtocolHandler extends SimpleChannelInboundHandler<HttpProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpProtocol msg) throws Exception {
        final HttpProtocol.CmdEnum cmd = msg.getCmd();
        switch (cmd){
            case REGISTER:
                final String registerName = msg.getRegisterName();
                final String[] names = registerName.split(",");
                for (String p : names) {
                    Channel channel = ChannelGroupKit.find(p);
                    if (channel != null) {
                        if (channel.isActive() && channel.isWritable()) {
                            final HttpProtocol failMsg = new HttpProtocol();
                            failMsg
                                    .setCmd(HttpProtocol.CmdEnum.REGISTER)
                                    .setId(msg.getId())
                                    .setBytes(("路由:" + p + "已被其他链接使用").getBytes(StandardCharsets.UTF_8));
                            ctx.writeAndFlush(failMsg);
                        } else {
                            ChannelGroupKit.remove(p);
                            channel.close();
                        }
                        return;
                    } else {
                        ChannelGroupKit.add(p, ctx.channel());
                        log.info("register client path {} successful", p);
                    }
                }
                final HttpProtocol successMsg = new HttpProtocol();
                successMsg
                        .setCmd(HttpProtocol.CmdEnum.REGISTER)
                        .setId(msg.getId())
                        .setBytes("注册成功".getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(successMsg);
                break;
            case RESPONSE:
                Long requested = msg.getId();
                Channel channel = CacheUtil.get(requested);
                if (channel != null && channel.isActive()) {
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.valueOf(msg.getVersion()), HttpResponseStatus.valueOf(msg.getCode()));
                    final byte[] bytes = msg.getBytes();
                    resp.content().writeBytes(bytes);
                    channel.writeAndFlush(resp).addListener(t -> {
                    });
                    CacheUtil.remove(requested);
                }
                break;
            default:
        }

    }
}
