package com.github.fishlikewater.server.handle;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.server.kit.CacheUtil;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月12日 22:16
 * @since
 **/
@Slf4j
public class ProxyHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static void main(String[] args) {
        String url = "";
        final String[] split = url.split("/");
        System.out.println(split.length);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            HttpHeaders headers = req.headers();
            String uri = req.uri();//headers.get("Host");
            /* 获取连接目标路由*/
            if (StrUtil.isBlank(uri)) {
                return;
            }
            final String[] split = uri.split("/");
            String triger = split[1];
            Channel channel = null;
            if (StrUtil.isBlank(triger)) {
                channel = ChannelGroupKit.find("default");
            } else {
                channel = ChannelGroupKit.find(triger);
                if (channel == null) {
                    channel = ChannelGroupKit.find("default");
                }
            }
            uri = uri.replace("/" + triger, "");
            if (channel == null) {
                byte[] bytes = "没有穿透路由".getBytes(Charset.defaultCharset());
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                resp.content().writeBytes(bytes);
                resp.headers().set("Content-Type", "text/html;charset=UTF-8");
                resp.headers().setInt("Content-Length", resp.content().readableBytes());
                ctx.writeAndFlush(resp);
            } else {
                MessageProbuf.Request.Builder builder = MessageProbuf.Request.newBuilder();
                builder.setHttpVersion(req.protocolVersion().text());
                builder.setUrl(uri);
                builder.setMethod(req.method().name());
                Map<String, String> header = new HashMap<>();
                headers.entries().forEach(t -> {
                    header.put(t.getKey(), t.getValue());
                });
                builder.putAllHeader(header);
                ByteBuf content = req.content();
                if (content.hasArray()) {
                    builder.setBody(ByteString.copyFrom(content.array()));
                } else {
                    byte[] bytes = new byte[content.readableBytes()];
                    content.readBytes(bytes);
                    builder.setBody(ByteString.copyFrom(bytes));
                }
                String requestId = IdUtil.next();
                channel.writeAndFlush(MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.REQUEST)
                        .setProtocol(MessageProbuf.Protocol.HTTP)
                        .setRequest(builder.build())
                        .setRequestId(requestId)).addListener((f) -> {
                    if (f.isSuccess()) {
                        CacheUtil.put(requestId, ctx.channel(), 300);
                    } else {
                        log.info("转送失败");
                    }
                });
                builder = null;
            }


        } else {
            log.info("not found http or https request, will close this channel");
            ctx.close();
        }
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
