package com.github.fishlikewater.proxy.handler.proxy_server;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 8:18
 * @since 客户端与服务端建立连接
 **/
@Slf4j
public class ProxyProtobufServerHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private static final AttributeKey<String> CLIENT_PATH = AttributeKey.valueOf("client_path");

    private ConnectionValidate connectionValidate;

    private ProxyConfig proxyConfig;

    public ProxyProtobufServerHandler(ConnectionValidate connectionValidate, ProxyConfig proxyConfig){
        this.connectionValidate = connectionValidate;
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        Attribute<String> attr = ctx.channel().attr(CLIENT_PATH);
        MessageProbuf.MessageType type = msg.getType();
        /** 连接验证*/
        if (type == MessageProbuf.MessageType.VALID) {
            MessageProbuf.Register register = msg.getRegister();
            boolean validate = connectionValidate.validate(register.getToken(), proxyConfig.getToken());
            if (!validate) {
                log.info("valid fail");
                ctx.close();
            }
            log.info("valid successful");
            /** 路由*/
            String path = register.getPath();
            if (StringUtils.isEmpty(path)) {
                /** 没有注册路由的无效连接*/
                ctx.close();
            } else {
                if(StringUtils.isEmpty(attr.get())){
                    attr.setIfAbsent(path);
                }
                ChannelGroupKit.add(path, ctx.channel());
            }
        } else {
            if (StringUtils.isEmpty(attr.get())) {
               /** 连接后没有经过验证的请求 直接关闭*/
                ctx.close();
                return;
            }
            switch (type) {
                case RESPONSE:
                    String requestid = msg.getRequestId();
                    Channel channel = CacheUtil.get(requestid);
                    if(channel != null && channel.isActive()){
                        ChannelPipeline pipeline = channel.pipeline();
                        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                        MessageProbuf.Response response = msg.getResponse();
                        response.getHeaderMap().entrySet().forEach(t->{
                            resp.headers().set(t.getKey(), t.getValue());
                        });
                        resp.content().writeBytes(response.getBody().toByteArray());
                        resp.setStatus(HttpResponseStatus.valueOf(response.getCode()));
                        channel.writeAndFlush(resp).addListener(t->{
                            //resp.release();
                        });
                        CacheUtil.remove(requestid);
                    }
                    break;
                case HEALTH:
                    break;
                default:
                    log.info("接收到不支持的消息类型");
                    //ctx.channel().writeAndFlush(MessageProbuf.Message.newBuilder().setType())
            }
        }
    }


    /**
     * 服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
    }

    /**
     * 客户端与服务端断开连接的时候调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * 每当从服务端收到新的客户端连接时， 客户端的 Channel 存入ChannelGroup列表中，
     * ChannelHandler添加到实际上下文中准备处理事件
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     * 将ChannelHandler从实际上下文中删除，不再处理事件
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Attribute<String> attr = ctx.channel().attr(CLIENT_PATH);
        String path = attr.get();
        if (!StringUtils.isEmpty(path)) {
            ChannelGroupKit.remove(path);
        }
        //ChannelGroupKit.remove(ctx.channel());
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
