package com.github.fishlikewater.proxy.handler.proxy_client;


import com.github.fishlikewater.proxy.boot.NettyProxyClient;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ClientHandlerInitializer
 * @Description
 * @date 2018年12月25日 15:05
 **/
public class ClientHandlerInitializer extends ChannelInitializer<Channel> {

    private ProxyConfig proxyConfig;

    private NettyProxyClient client;

    public ClientHandlerInitializer(ProxyConfig proxyConfig, NettyProxyClient client) {
        this.proxyConfig = proxyConfig;
        this.client = client;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()))
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS))
                .addLast(new HeartBeatHandler(client))
                .addLast(new ClientMessageHandler(client));


    }
}
