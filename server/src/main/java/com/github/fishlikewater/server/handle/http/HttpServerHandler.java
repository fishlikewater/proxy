package com.github.fishlikewater.server.handle.http;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.codec.HttpProtocol;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.server.kit.CacheUtil;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since: 2019年07月12日 22:16
 **/
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof FullHttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            HttpHeaders headers = req.headers();
            String uri = req.uri();
            String path = headers.get("path");
            if (StrUtil.isBlank(path)) {
                if (StrUtil.isBlank(uri)) {
                    path = "default";
                }else {
                    final String[] split = uri.split("/");
                    path = split[1];
                }
            }
            Channel channel;
            if (StrUtil.isNotBlank(path)) {
                channel = ChannelGroupKit.find(path);
                if (Objects.isNull(channel)) {
                    path = "default";
                    channel = ChannelGroupKit.find(path);
                }
            } else {
                path = "default";
                channel = ChannelGroupKit.find("default");
            }
            if (channel == null) {
                byte[] bytes = "没有穿透路由".getBytes(Charset.defaultCharset());
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                resp.content().writeBytes(bytes);
                resp.headers().set("Content-Type", "text/html;charset=UTF-8");
                resp.headers().setInt("Content-Length", resp.content().readableBytes());
                ctx.writeAndFlush(resp);
            } else {
                final HttpProtocol httpProtocol = new HttpProtocol();
                Long requestId = IdUtil.id();
                httpProtocol.setDstServer(path);
                httpProtocol.setCmd(HttpProtocol.CmdEnum.REQUEST);
                httpProtocol.setId(requestId);
                httpProtocol.setBytes(req.content().array());
                httpProtocol.setHeads(req.headers());
                httpProtocol.setUrl(uri);
                httpProtocol.setMethod(req.method().name());
                httpProtocol.setVersion(req.protocolVersion().text());
                ctx.channel().attr(ChannelGroupKit.CHANNELS_LOCAL).set(requestId);
                channel.writeAndFlush(httpProtocol).addListener((f) -> {
                    if (f.isSuccess()) {
                        CacheUtil.put(requestId, ctx.channel(), 300);
                    } else {
                        log.info("转送失败");
                    }

                });
            }
        } else {
            log.info("not found http or https request, will close this channel");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Attribute<Long> attr = ctx.channel().attr(ChannelGroupKit.CHANNELS_LOCAL);
        if (attr != null) {
            CacheUtil.remove(attr.get());
        }
        super.channelInactive(ctx);
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
