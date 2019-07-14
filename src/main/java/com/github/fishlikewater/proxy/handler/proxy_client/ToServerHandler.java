package com.github.fishlikewater.proxy.handler.proxy_client;

import com.alibaba.fastjson.JSON;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.github.fishlikewater.proxy.kit.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 13:57
 * @since
 **/
public class ToServerHandler extends SimpleChannelInboundHandler {

    private Channel outChannel;

    private String requestId;

    public ToServerHandler(Channel outChannel, String requestId) {
        this.outChannel = outChannel;
        this.requestId = requestId;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("交换数据");
        if(msg instanceof FullHttpResponse){
            FullHttpResponse resp = (FullHttpResponse) msg;
            Response response = new Response();
            int code = resp.status().code();
            Map<String, String> header = new HashMap<>();
            resp.headers().entries().forEach(t->{
                header.put(t.getKey(), t.getValue());
            });
            response.setBody(resp.content().toString(CharsetUtil.UTF_8).getBytes());
            response.setCode(code);
            response.setHeader(header);
            outChannel.write(MessageProbuf.Message.newBuilder()
                    .setRequestid(requestId)
                    .setBody(JSON.toJSONString(response))
                    .setType(MessageProbuf.MessageType.RESULT)).addListener(t->{
            });
            response = null;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        outChannel.flush();
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
