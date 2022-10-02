package com.github.fishlikewater.proxy.kit;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.proxy_server.CacheUtil;
import com.github.fishlikewater.proxy.handler.proxy_server.ConnectionValidate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
                                      Attribute<String> attr, ConnectionValidate connectionValidate,
                                      ProxyConfig proxyConfig) {
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
        if (StringUtils.isEmpty(path)) {
            /* 没有注册路由的无效连接*/
            ChannelGroupKit.sendVailFail(ctx.channel(), "请配置路由");
        } else {
            if (extend.equals("client")){
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
            if (extend.equals("call")){
                Channel channel = ChannelGroupKit.findCall(path);
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
        }
    }

    public static void handleTcp(ChannelHandlerContext ctx, MessageProbuf.Message msg, MessageProbuf.MessageType type) {
        final String path = msg.getRequestId();
        switch (type) {
            case REQUEST:
                Channel channel = ChannelGroupKit.find(path);
                if (Objects.isNull(channel)) {
                    MessageProbuf.Message respFailVailMsg = MessageProbuf.Message.newBuilder()
                            .setType(MessageProbuf.MessageType.RESPONSE)
                            .setProtocol(MessageProbuf.Protocol.TCP)
                            .setExtend("没有指定path的客户端注册").build();
                    ctx.writeAndFlush(respFailVailMsg);
                    return;
                }
                MessageProbuf.Message req = MessageProbuf.Message.newBuilder()
                        .setType(MessageProbuf.MessageType.REQUEST)
                        .setProtocol(MessageProbuf.Protocol.TCP)
                        .build();
                channel.writeAndFlush(req);
                break;
            case RESPONSE:
                Channel callChannel = ChannelGroupKit.findCall(path);
                if (Objects.isNull(callChannel)) {
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
