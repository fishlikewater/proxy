package com.github.fishlikewater.server.handle.myprotocol;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.kit.IdUtil;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.ServerHeartBeatHandler;
import com.github.fishlikewater.server.handle.vpn.VpnMessageHandler;
import com.github.fishlikewater.server.handle.vpn.VpnRegisterHandler;
import com.github.fishlikewater.server.kit.ChannelGroupKit;
import com.github.fishlikewater.server.kit.ConnectionValidate;
import com.github.fishlikewater.server.kit.IpMapping;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.github.fishlikewater.server.kit.ChannelGroupKit.DATA_CHANNEL;

/**
 * <p>
 * token验证处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月19日 14:25
 **/
@RequiredArgsConstructor
public class AuthHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ConnectionValidate connectionValidate;
    private final ProxyConfig proxyConfig;
    private final IpMapping ipMapping;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) {
        switch (msg.getCmd()) {
            case DATA_CHANNEL:
                handleDataChannel(ctx, msg);
                break;
            case AUTH:
                handleAuth(ctx, msg);
                break;
            default:
                sendFailMsg(ctx, "请先验证token", msg.getId());
                break;
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, MessageProtocol msg) {
        //处理 连接安全验证
        final String token = new String(msg.getBytes(), StandardCharsets.UTF_8);
        boolean validate = connectionValidate.validate(token, proxyConfig.getToken());
        if (validate) {
            ctx.pipeline().remove(this);
            sendSuccessAuthMsg(ctx, msg.getId());
        } else {
            sendFailMsg(ctx, "验证失败", msg.getId());
        }
    }

    private void handleDataChannel(ChannelHandlerContext ctx, MessageProtocol msg) {
        if (msg.getState() == 0) {
            final String token = new String(msg.getBytes(), StandardCharsets.UTF_8);
            boolean validate = connectionValidate.validate(token, proxyConfig.getToken());
            if (!validate) {
                sendFailMsg(ctx, "验证失败", msg.getId());
                return;
            }
        }
        final String id = ctx.channel().id().asLongText();
        ChannelGroupKit.add(id, ctx.channel());
        if (msg.getState() == 0) {
            final String linkIp = msg.getDst().getDstAddress();
            final Channel channel = ipMapping.getChannel(linkIp);
            if (Objects.nonNull(channel) && channel.isActive()) {
                //发送消息 让目标机 建立一条新的连接用于数据交互
                MessageProtocol messageProtocol = getMsg(id, IdUtil.id(), MessageProtocol.CmdEnum.DATA_CHANNEL);
                channel.writeAndFlush(messageProtocol);
            }
        } else {
            final String mainId = new String(msg.getBytes(), StandardCharsets.UTF_8);
            final Channel mainChannel = ChannelGroupKit.find(mainId);
            if (Objects.nonNull(mainChannel) && mainChannel.isActive()) {
                mainChannel.attr(DATA_CHANNEL).set(ctx.channel());
                ctx.channel().attr(DATA_CHANNEL).set(mainChannel);
                MessageProtocol ack = getMsg("数据通道建立成功", IdUtil.id(), MessageProtocol.CmdEnum.DATA_CHANNEL_ACK);
                ackMsg(mainChannel, ack);
                ackMsg(ctx.channel(), ack);
            }
        }
    }

    private void sendFailMsg(ChannelHandlerContext ctx, String message, Long id) {
        final MessageProtocol failMsg = new MessageProtocol();
        failMsg
                .setCmd(MessageProtocol.CmdEnum.AUTH)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setId(id)
                .setState((byte) 0)
                .setBytes(message.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(failMsg);
    }

    private void sendSuccessAuthMsg(ChannelHandlerContext ctx, Long id) {
        MessageProtocol successMsg = new MessageProtocol()
                .setCmd(MessageProtocol.CmdEnum.AUTH)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setId(id)
                .setState((byte) 1);
        ctx.writeAndFlush(successMsg);
    }

    private MessageProtocol getMsg(String msg, Long id, MessageProtocol.CmdEnum cmd) {
        final MessageProtocol messageProtocol = new MessageProtocol();
        return messageProtocol
                .setId(id)
                .setCmd(cmd)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(msg.getBytes(StandardCharsets.UTF_8));
    }

    private void ackMsg(Channel channel, MessageProtocol ack) {
        channel.writeAndFlush(ack).addListener(future -> {
            if (future.isSuccess()) {
                final ChannelPipeline pipeline = channel.pipeline();
                pipeline.remove(LengthFieldBasedFrameDecoder.class);
                pipeline.remove(MyByteToMessageCodec.class);
                pipeline.remove(VpnMessageHandler.class);
                pipeline.remove(IdleStateHandler.class);
                pipeline.remove(ServerHeartBeatHandler.class);
                pipeline.remove(VpnRegisterHandler.class);
                if (pipeline.get(AuthHandler.class) != null) {
                    pipeline.remove(AuthHandler.class);
                }
                pipeline.addLast(new DataTransferHandler());
            }
        });
    }
}
