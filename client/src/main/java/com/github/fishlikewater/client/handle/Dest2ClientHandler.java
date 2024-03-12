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
package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年10月18日 22:16
 */
@Slf4j
public class Dest2ClientHandler extends SimpleChannelInboundHandler<byte[]> {

    private final ChannelHandlerContext clientChannelContext;
    private final Long requestId;
    private final MessageProtocol.Dst dst;

    public Dest2ClientHandler(ChannelHandlerContext clientChannelContext, Long requestId, MessageProtocol.Dst dst) {
        this.clientChannelContext = clientChannelContext;
        this.requestId = requestId;
        this.dst = dst;
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        boolean canWrite = ctx.channel().isWritable();
        log.trace(ctx.channel() + " 可写性：" + canWrite);
        //流量控制，不允许继续读
        clientChannelContext.channel().config().setAutoRead(canWrite);
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] destMsg) {
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol
                .setId(requestId)
                .setDst(dst)
                .setCmd(MessageProtocol.CmdEnum.RESPONSE)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(destMsg);
        clientChannelContext.writeAndFlush(messageProtocol);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.trace("目标服务器断开连接");
        ctx.close();
        final MessageProtocol closeMsg = new MessageProtocol();
        closeMsg
                .setId(requestId)
                .setCmd(MessageProtocol.CmdEnum.CLOSE)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS);
        clientChannelContext.writeAndFlush(closeMsg);
        clientChannelContext.channel().attr(ChannelKit.CHANNELS_LOCAL).get().remove(requestId);
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
