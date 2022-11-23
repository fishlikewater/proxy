package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.BootStrapFactory;
import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.kit.IdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月20日 14:40
 **/
@Slf4j
public class HandleKit {


    public static void createDataChannel(ChannelHandlerContext ctx, ProxyConfig proxyConfig, String mainChannelId) throws InterruptedException {
        final Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline
                        .addLast(new LengthFieldBasedFrameDecoder(5 * 1024 * 1024, 0, 4))
                        .addLast(new MyByteToMessageCodec())
                        .addLast(new ClientDataHandler());
            }
        });
        bootstrap.remoteAddress(proxyConfig.getAddress(), proxyConfig.getPort());
        ChannelFuture future = bootstrap.connect().sync();
        //连接成功后 发送消息 表明需要建立数据通道
        final MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol
                .setId(IdUtil.id())
                .setState((byte) 1)
                .setCmd(MessageProtocol.CmdEnum.DATA_CHANNEL)
                .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                .setBytes(mainChannelId.getBytes(StandardCharsets.UTF_8));
        future.channel().writeAndFlush(messageProtocol).addListener(future1 -> {
            if (future1.isSuccess())
            {
                log.info("成功发送建立数据通道请求...");
            }
        });
    }


}
