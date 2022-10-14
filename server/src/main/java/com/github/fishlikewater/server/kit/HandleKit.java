package com.github.fishlikewater.server.kit;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.ConnectionValidate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月01日 16:37
 **/
@Slf4j
public class HandleKit {

    public static void handleHttp(ChannelHandlerContext ctx, MessageProbuf.Message msg, MessageProbuf.MessageType type) {
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

    public static void handleRegister(ChannelHandlerContext ctx, MessageProbuf.Message msg,
                                      ConnectionValidate connectionValidate, ProxyConfig proxyConfig) {
        MessageProbuf.Register register = msg.getRegister();
        final String extend = msg.getExtend();
        boolean validate = connectionValidate.validate(register.getToken(), proxyConfig.getToken());
        if (!validate) {
            log.info("valid fail");
            ChannelGroupKit.sendVailFail(ctx.channel(), "token验证失败");
            return;
        }
        log.info("valid successful");
        /* 路由*/
        String path = register.getPath();
        if (StrUtil.isEmpty(path)) {
            /* 没有注册路由的无效连接*/
            ChannelGroupKit.sendVailFail(ctx.channel(), "请配置路由");
        } else {
            if (extend.equals("client")){
                Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.CLIENT_PATH);
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
                if(StrUtil.isEmpty(attr.get())){
                    log.info("set client path {} successful", path);
                    attr.setIfAbsent(path);
                }
                ChannelGroupKit.add(path, ctx.channel());
                ChannelGroupKit.sendVailSuccess(ctx.channel());
                log.info("register client path {} successful", path);
            }
            if (extend.equals("call")){
                Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.CALL_CLIENT);
                final String requestId = msg.getRequestId();
                ChannelGroupKit.addCall(requestId, ctx.channel());
                attr.set(requestId);
                ChannelGroupKit.sendVailSuccess(ctx.channel());
                log.info("register call client requestId {} successful", requestId);

            }
        }
    }

    public static void handleTcp(ChannelHandlerContext ctx, MessageProbuf.Message msg, MessageProbuf.MessageType type) {
        if (type == MessageProbuf.MessageType.REQUEST || type == MessageProbuf.MessageType.INIT ||  type == MessageProbuf.MessageType.CLOSE){
            Channel channel = ChannelGroupKit.find(msg.getExtend());
            if (Objects.isNull(channel)) {
                log.warn("没有指定path的客户端注册");
                return;
            }
            channel.writeAndFlush(msg);
        }
        if (type == MessageProbuf.MessageType.RESPONSE){
            Channel callChannel = ChannelGroupKit.findCall(msg.getRequestId());
            if (Objects.isNull(callChannel)) {
                log.info("调用方已离线");
                MessageProbuf.Message respFailVailMsg = MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.RESPONSE)
                        .setProtocol(MessageProbuf.Protocol.TCP)
                        .setExtend("调用方已离线").build();
                ctx.writeAndFlush(respFailVailMsg);
            }else {
                callChannel.writeAndFlush(msg);
            }
        }
    }

}
