package com.github.fishlikewater.server.handle.myprotocol;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 处理自定义协议
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月19日 11:07
 **/
@Slf4j
public class MyProtocolHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        switch (cmd) {
            case REGISTER: //处理注册
                final byte[] bytes = msg.getBytes();
                final String registerName = new String(bytes, StandardCharsets.UTF_8);
                final byte registerType = msg.getState();
                // 未提供注册名(注册名对于受控制机全局唯一，为方便使用端 采用自定义设置)
                if (StrUtil.isBlank(registerName)) {
                    final MessageProtocol failMsg = new MessageProtocol();
                    failMsg
                            .setId(msg.getId())
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setState((byte) 0)
                            .setBytes("请配置路由".getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(failMsg).addListener(future -> ctx.close());
                    break;
                }
                // 受控制注册
                if (registerType == 1) {
                    // 查询 注册名是否已经被使用
                    final Channel channel = ChannelGroupKit.find(registerName);
                    if (channel != null && channel.isActive()) {
                        final MessageProtocol failMsg = new MessageProtocol();
                        failMsg
                                .setId(msg.getId())
                                .setCmd(MessageProtocol.CmdEnum.REGISTER)
                                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                                .setState((byte) 0)
                                .setBytes("该注册名已被使用, 请更改注册名后重新连接".getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(failMsg).addListener(future -> ctx.close());
                        break;
                    }
                    // 如果 存在已注册 但不是活动连接 清理掉该连接
                    if (channel != null && !channel.isActive()) {
                        ChannelGroupKit.remove(registerName);
                        channel.close();
                    }
                    ctx.channel().attr(ChannelGroupKit.CLIENT_PATH).set(registerName);
                    ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).set("client");
                    ChannelGroupKit.add(registerName, ctx.channel());
                    final MessageProtocol successMsg = new MessageProtocol();
                    successMsg
                            .setId(msg.getId())
                            .setCmd(MessageProtocol.CmdEnum.REGISTER)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setState((byte) 1)
                            .setBytes("注册成功".getBytes(StandardCharsets.UTF_8));
                    ctx.writeAndFlush(successMsg);
                    log.info("register client path {} successful", registerName);
                } else { // 呼叫机注册
                    ChannelGroupKit.add(ctx.channel().id().asLongText(), ctx.channel());
                    Channel channel = ChannelGroupKit.find(registerName);
                    if (channel != null) {
                        ctx.channel().attr(ChannelGroupKit.CALL_REMOTE_CLIENT).set(channel);
                        channel.attr(ChannelGroupKit.CALL_REQUEST_CLIENT).set(ctx.channel());
                        ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).set("call");
                        final MessageProtocol successMsg = new MessageProtocol();
                        successMsg
                                .setId(msg.getId())
                                .setCmd(MessageProtocol.CmdEnum.REGISTER)
                                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                                .setState((byte) 1)
                                .setBytes(ctx.channel().id().asLongText().getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(successMsg);
                        log.info("register call client requestId {} successful", msg.getId());
                    } else {
                        final MessageProtocol failMsg = new MessageProtocol();
                        failMsg
                                .setId(msg.getId())
                                .setCmd(MessageProtocol.CmdEnum.REGISTER)
                                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                                .setState((byte) 0)
                                .setBytes("未匹配到目标机".getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(failMsg);
                    }
                }
                break;

            case HEALTH:
                log.debug("get client health info");
                break;
            default:
                final String type = ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).get();
                if (type.equals("client"))
                {
                    final Channel callChannel = ctx.channel().attr(ChannelGroupKit.CALL_REQUEST_CLIENT).get();
                    if (callChannel != null && callChannel.isActive() && callChannel.isWritable()){
                        callChannel.writeAndFlush(msg);
                    }
                }else
                {
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

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     * 将ChannelHandler从实际上下文中删除，不再处理事件
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final String type = ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).get();
        if (type.equals("client"))
        {
            log.info("受控制机断开连接");
            Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.CLIENT_PATH);
            if(attr != null)
            {
                String path = attr.get();
                if (StrUtil.isNotBlank(path))
                {
                    log.info(path + "断开连接");
                    log.info("close chanel and clean path {}", path);
                    ChannelGroupKit.remove(path);
                }
            }
        }else
        {
            log.info("呼叫机断开连接");
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
