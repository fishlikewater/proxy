package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.codec.HttpProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月13日 13:57
 **/
@Slf4j
@RequiredArgsConstructor
public class ToServerHandler extends SimpleChannelInboundHandler<Object> {

    private final Long requested;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse resp = (FullHttpResponse) msg;
            int code = resp.status().code();
            final HttpProtocol httpProtocol = new HttpProtocol();
            //允许跨域访问
            resp.headers().add("Access-Control-Allow-Origin", "*");
            resp.headers().add("Access-Control-Allow-Methods", "*");
            resp.headers().add("Access-Control-Allow-Headers", "*");
            resp.headers().add("ACCESS-CONTROL-ALLOW-CREDENTIALS", "true");
            ByteBuf content = resp.content();
            httpProtocol.setBytes(resp.content().array());
            if (content.hasArray()) {
                httpProtocol.setBytes(resp.content().array());
            } else {
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                httpProtocol.setBytes(bytes);
            }
            httpProtocol.setHeads(resp.headers());
            httpProtocol.setCode(code);
            httpProtocol.setId(requested);
            httpProtocol.setCmd(HttpProtocol.CmdEnum.RESPONSE);
            ChannelKit.sendMessage(httpProtocol, t -> {

            });
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("happen error: ", cause);
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
