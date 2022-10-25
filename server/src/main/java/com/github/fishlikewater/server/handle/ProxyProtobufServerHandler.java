package com.github.fishlikewater.server.handle;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.HandleKit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 8:18
 * @since 客户端与服务端建立连接
 **/
@Slf4j
public class ProxyProtobufServerHandler extends SimpleChannelInboundHandler<MessageProbuf.Message> {

    private final ConnectionValidate connectionValidate;

    private final ProxyConfig proxyConfig;

    public ProxyProtobufServerHandler(ConnectionValidate connectionValidate, ProxyConfig proxyConfig){
        this.connectionValidate = connectionValidate;
        this.proxyConfig = proxyConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProbuf.Message msg) {
        MessageProbuf.MessageType type = msg.getType();
        final MessageProbuf.Protocol protocol = msg.getProtocol();
        /* 连接验证*/
        if (type == MessageProbuf.MessageType.VALID) {
            HandleKit.handleRegister(ctx, msg, connectionValidate, proxyConfig);
        } else {
            /* 检验是否通过验证*/
            final String clientType = ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).get();
            if (StrUtil.isBlank(clientType)){
                log.warn("收到非法请求: {}", ctx.channel().remoteAddress().toString());
                ctx.close();
                return;
            }
            if (protocol == MessageProbuf.Protocol.HTTP){
                HandleKit.handleHttp(ctx, msg, type);
            }
            if (protocol == MessageProbuf.Protocol.TCP){
                HandleKit.handleTcp(ctx, msg, type);
            }
            if (protocol == MessageProbuf.Protocol.SOCKS){
                HandleKit.handleSocks(ctx, msg);
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
        super.handlerAdded(ctx);
    }

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     * 将ChannelHandler从实际上下文中删除，不再处理事件
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.CLIENT_PATH);
        if(attr != null){
            String path = attr.get();
            if (StrUtil.isNotBlank(path)) {
                log.info(path + "断开连接");
                log.info("close chanel and clean path {}", path);
                ChannelGroupKit.remove(path);
            }
        }
        final Attribute<String> attribute = ctx.channel().attr(ChannelGroupKit.CALL_FLAG);
        if (attribute != null){
            final String requestId = attribute.get();
            if (StrUtil.isNotBlank(requestId)) {
                log.info(requestId + "断开连接");
                log.info("close chanel and clean requestId {}", requestId);
                ChannelGroupKit.removeCall(requestId);
            }
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
