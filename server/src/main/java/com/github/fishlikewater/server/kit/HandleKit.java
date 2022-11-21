package com.github.fishlikewater.server.kit;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.server.config.ProxyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

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
                Long requested = msg.getRequestId();
                Channel channel = CacheUtil.get(requested);
                if (channel != null && channel.isActive()) {
                    //ChannelPipeline pipeline = channel.pipeline();
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                    MessageProbuf.Response response = msg.getResponse();
                    response.getHeaderMap().forEach((key, value) -> resp.headers().set(key, value));
                    resp.content().writeBytes(response.getBody().toByteArray());
                    resp.setStatus(HttpResponseStatus.valueOf(response.getCode()));
                    channel.writeAndFlush(resp).addListener(t -> {
                        //resp.release();
                    });
                    CacheUtil.remove(requested);
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
        boolean validate = connectionValidate.validate(register.getToken(), proxyConfig.getToken());
        if (!validate) {
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
            //处理http穿透 多服务注册
            if (path.contains(",")) {
                final String[] split = path.split(",");
                for (String p : split) {
                    Channel channel = ChannelGroupKit.find(p);
                    if (channel != null) {
                        if (channel.isActive() && channel.isWritable()) {
                            ChannelGroupKit.sendVailFail(ctx.channel(), "路由:" + p + "已被其他链接使用");
                        } else {
                            ChannelGroupKit.remove(p);
                            channel.close();
                        }
                        return;
                    } else {
                        ChannelGroupKit.add(p, ctx.channel());
                        ChannelGroupKit.sendVailSuccess(ctx.channel());
                        log.info("register client path {} successful", p);
                    }
                }
                ctx.channel().attr(ChannelGroupKit.CLIENT_PATH).set(path);
            } else {
                Channel channel = ChannelGroupKit.find(path);
                if (channel != null) {
                    if (channel.isActive() && channel.isWritable()) {
                        ChannelGroupKit.sendVailFail(ctx.channel(), "路由已被其他链接使用");
                    } else {
                        ChannelGroupKit.remove(path);
                        channel.close();
                    }
                } else {
                    ctx.channel().attr(ChannelGroupKit.CLIENT_PATH).set(path);
                    ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).set("client");
                    ChannelGroupKit.add(path, ctx.channel());
                    ChannelGroupKit.sendVailSuccess(ctx.channel());
                    log.info("register client path {} successful", path);
                }
            }

        }
    }

}
