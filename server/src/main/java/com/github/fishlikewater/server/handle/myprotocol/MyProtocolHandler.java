package com.github.fishlikewater.server.handle.myprotocol;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>
 * 处理自定义协议
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月19日 11:07
 **/
@Deprecated
@Slf4j
public class MyProtocolHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd == MessageProtocol.CmdEnum.HEALTH){
            log.debug("get client health info");
        }else {
            final String type = ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).get();
            if ("client".equals(type)) {
                final Channel callChannel = ctx.channel().attr(ChannelGroupKit.CALL_REQUEST_CLIENT).get();
                if (callChannel != null && callChannel.isActive() && callChannel.isWritable()) {
                    callChannel.writeAndFlush(msg);
                }
            } else {
                final Channel channel = ctx.channel().attr(ChannelGroupKit.CALL_REMOTE_CLIENT).get();
                if (channel != null && channel.isActive() && channel.isWritable()) {
                    channel.writeAndFlush(msg);
                }
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
