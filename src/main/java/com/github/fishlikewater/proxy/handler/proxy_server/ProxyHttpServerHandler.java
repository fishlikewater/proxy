package com.github.fishlikewater.proxy.handler.proxy_server;

import com.alibaba.fastjson.JSON;
import com.github.fishlikewater.proxy.kit.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            HttpHeaders headers = req.headers();
            String uri = headers.get("Host");
            /** 获取连接目标路由*/
            String triger = uri.split("\\.")[0];
            Channel channel = ChannelGroupKit.find(triger);
            if(channel == null){
                byte[] bytes = "没有穿透路由".getBytes(Charset.defaultCharset());
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                resp.content().writeBytes(bytes);
                resp.headers().set("Content-Type", "text/html;charset=UTF-8");
                resp.headers().setInt("Content-Length", resp.content().readableBytes());
                ctx.writeAndFlush(resp);
            }else {
                Request request = new Request();
                request.setHttpVersion(req.protocolVersion().text());
                request.setUrl(req.uri());
                request.setMethod(req.method().name());
                Map<String, String> header = new HashMap<>();
                headers.entries().forEach(t->{
                    header.put(t.getKey(), t.getValue());
                });
                request.setHeader(header);
                request.setBody(req.content().toString(CharsetUtil.UTF_8).getBytes());
                String requestId = IdUtil.next();
                String rmsg = JSON.toJSONString(request);
                channel.writeAndFlush(MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.CONNECTION)
                        .setLength(rmsg.getBytes().length)
                        .setBody(rmsg)
                        .setRequestid(requestId));
                CacheUtil.put(requestId, ctx.channel(), 3);
                request = null;
            }


        }else {
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
