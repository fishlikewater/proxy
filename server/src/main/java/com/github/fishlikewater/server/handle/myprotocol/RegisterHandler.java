/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.server.handle.myprotocol;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.server.config.Constant;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.HandleKit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月07日 15:50
 **/
@Deprecated
@Slf4j
public class RegisterHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        if (cmd == MessageProtocol.CmdEnum.REGISTER) {
            final byte[] bytes = msg.getBytes();
            final String registerName = new String(bytes, StandardCharsets.UTF_8);
            final byte registerType = msg.getState();
            // 未提供注册名(注册名对于受控制机全局唯一，为方便使用端 采用自定义设置)
            final boolean checkRegisterName = HandleKit.checkRegisterName(ctx, registerName, msg.getId());
            if (!checkRegisterName) {
                return;
            }
            // 受控制注册
            if (registerType == 1) {
                // 查询 注册名是否已经被使用
                final boolean b = HandleKit.checkRegisterNameIsUse(registerName, msg.getId(), ctx);
                if (b) {
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
                }
            } else { // 呼叫机注册
                ChannelGroupKit.add(ctx.channel().id().asLongText(), ctx.channel());
                Channel channel = ChannelGroupKit.find(registerName);
                if (channel != null) {
                    ctx.channel().attr(ChannelGroupKit.CALL_REMOTE_CLIENT).set(channel);
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
        }
    }


    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     * 将ChannelHandler从实际上下文中删除，不再处理事件
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final String type = ctx.channel().attr(ChannelGroupKit.CLIENT_TYPE).get();
        if (Constant.CLIENT.equals(type)) {
            log.info("受控制机断开连接");
            Attribute<String> attr = ctx.channel().attr(ChannelGroupKit.CLIENT_PATH);
            if (attr != null) {
                String path = attr.get();
                if (StrUtil.isNotBlank(path)) {
                    log.info(path + "断开连接");
                    log.info("close chanel and clean path {}", path);
                    ChannelGroupKit.remove(path);
                }
            }
        }
        if (Constant.CALL.equals(type)) {
            log.info("呼叫机断开连接");
        }
        super.handlerRemoved(ctx);
    }
}
