package com.github.fishlikewater.proxy.handler.proxy_server;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
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

    private final ConnectionValidate connectionValidate;

    private final ProxyConfig proxyConfig;

    public ProxyProtobufServerHandler(ConnectionValidate connectionValidate, ProxyConfig proxyConfig){
        this.connectionValidate = connectionValidate;
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        Attribute<String> attr = ctx.channel().attr(CLIENT_PATH);
        MessageProbuf.MessageType type = msg.getType();
        /* 连接验证*/
        if (type == MessageProbuf.MessageType.VALID) {
            MessageProbuf.Register register = msg.getRegister();
            boolean validate = connectionValidate.validate(register.getToken(), proxyConfig.getToken());
            if (!validate) {
                log.info("valid fail");
                ChannelGroupKit.sendVailFail(ctx.channel(), "token验证失败");
                return;
            }
            log.info("valid successful");
            /* 路由*/
            String path = register.getPath();
            if (StringUtils.isEmpty(path)) {
                /* 没有注册路由的无效连接*/
                ChannelGroupKit.sendVailFail(ctx.channel(), "请配置路由");
            } else {
                Channel channel = ChannelGroupKit.find(path);
                if(channel != null){
                    if(channel.isActive() && channel.isWritable()){
                        log.warn("this path {} is existed", path);
                        ChannelGroupKit.sendVailFail(ctx.channel(), "路由已被其他链接使用");
                        return;
                    }else {
                        ChannelGroupKit.remove(path);
                        channel.close();
                    }
                }
                if(StringUtils.isEmpty(attr.get())){
                    log.info("set path {} successful", path);
                    attr.setIfAbsent(path);
                }
                ChannelGroupKit.add(path, ctx.channel());
                ChannelGroupKit.sendVailSuccess(ctx.channel());
                log.info("register path {} successful", path);
            }
        } else {
            if (StringUtils.isEmpty(attr.get())) {
                /* 连接后没有经过验证的请求 直接关闭*/
                ChannelGroupKit.sendVailFail(ctx.channel(), "非法请求");
                return;
            }
            switch (type) {
                case RESPONSE:
                    String requestid = msg.getRequestId();
                    Channel channel = CacheUtil.get(requestid);
                    if(channel != null && channel.isActive()){
                        //ChannelPipeline pipeline = channel.pipeline();
                        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                        MessageProbuf.Response response = msg.getResponse();
                        response.getHeaderMap().forEach((key, value) -> resp.headers().set(key, value));
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
                case CLOSE:
                    ctx.close();
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
        log.info(ctx.channel().remoteAddress().toString() + "断开连接");
        super.channelInactive(ctx);
    }

    /**
     * 每当从服务端收到新的客户端连接时， 客户端的 Channel 存入ChannelGroup列表中，
     * ChannelHandler添加到实际上下文中准备处理事件
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelGroupKit.add(ctx.channel());
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
        log.info(path + "断开连接");
        if (!StringUtils.isEmpty(path)) {
            log.info("close chanel and clean path {}", path);
            ChannelGroupKit.remove(path);
        }
        super.handlerRemoved(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("远程发送异常");
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
