package com.github.fishlikewater.server.kit;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.codec.MessageProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年10月01日 16:37
 **/
@Slf4j
public class HandleKit {

    public static boolean checkRegisterName(ChannelHandlerContext ctx, String registerName, long requestId) {
        if (StrUtil.isBlank(registerName)) {
            final MessageProtocol failMsg = new MessageProtocol();
            failMsg
                    .setId(requestId)
                    .setCmd(MessageProtocol.CmdEnum.REGISTER)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setState((byte) 0)
                    .setBytes("请配置路由".getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(failMsg).addListener(future -> ctx.close());
            return false;
        }
        return true;
    }


    public static boolean checkRegisterNameIsUse(String registerName, long requestId, ChannelHandlerContext ctx){
        final Channel channel = ChannelGroupKit.find(registerName);
        if (channel != null && channel.isActive()) {
            final MessageProtocol failMsg = new MessageProtocol();
            failMsg
                    .setId(requestId)
                    .setCmd(MessageProtocol.CmdEnum.REGISTER)
                    .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                    .setState((byte) 0)
                    .setBytes("该注册名已被使用, 请更改注册名后重新连接".getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(failMsg).addListener(future -> ctx.close());
            return false;
        }
        // 如果 存在已注册 但不是活动连接 清理掉该连接
        if (channel != null && !channel.isActive()) {
            ChannelGroupKit.remove(registerName);
            channel.close();
            return false;
        }
        return true;
    }
}
