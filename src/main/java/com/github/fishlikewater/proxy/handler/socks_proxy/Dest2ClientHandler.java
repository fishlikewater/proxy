package com.github.fishlikewater.proxy.handler.socks_proxy;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.proxy_server.CacheUtil;
import com.github.fishlikewater.proxy.handler.proxy_server.ConnectionValidate;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.IpCacheKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import com.google.protobuf.ByteString;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @date: 2022年07月29日 14:42
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
public class Dest2ClientHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private static final AtomicInteger ips = new AtomicInteger(1);

    private final ConnectionValidate connectionValidate;

    private final ProxyConfig proxyConfig;

    public Dest2ClientHandler(ConnectionValidate connectionValidate, ProxyConfig proxyConfig){
        this.connectionValidate = connectionValidate;
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) throws Exception {
        MessageProbuf.MessageType type = msg.getType();
        switch (type){
            case RESPONSE:
                String requestid = msg.getRequestId();
                Channel channel = CacheUtil.get(requestid);
                if(channel != null && channel.isActive()){
                    MessageProbuf.Response response = msg.getResponse();
                    final Object message = ObjectUtil.unserialize(response.getBody().toByteArray());
                    channel.writeAndFlush(message).addListener(t->{
                        //resp.release();
                    });
                    CacheUtil.remove(requestid);
                }
                break;
            case VALID:
                log.info("valid");
                MessageProbuf.Register register = msg.getRegister();
                boolean validate = connectionValidate.validate(register.getToken(), proxyConfig.getToken());
                if (!validate) {
                    log.info("valid fail");
                    ChannelGroupKit.sendVailFail(ctx.channel(), "token验证失败");
                }else {
                    if (ips.get() > 254){
                        ips.set(1);
                    }
                    String ip;
                    while (true){
                        final int i = ips.get();
                        ip = proxyConfig.getIp() + "." +i;
                        final boolean b = IpCacheKit.findByIp(ip);
                        if (b){
                            ips.incrementAndGet();
                        }else {
                            break;
                        }
                    }
                    IpCacheKit.add(ip, ctx.channel());
                    log.info("分配ip地址:{}", ip);
                    ips.incrementAndGet();
                    MessageProbuf.Response.Builder builder = MessageProbuf.Response.newBuilder();
                    ctx.writeAndFlush(MessageProbuf.Message.newBuilder()
                            .setType(MessageProbuf.MessageType.RESPONSE)
                            .setResponse(builder.build())
                            .setExtend(ip));
                }
                break;
            case HEALTH:
                //log.info("获取心跳包");
                break;
        }
        ctx.fireChannelRead(msg);

    }
}
