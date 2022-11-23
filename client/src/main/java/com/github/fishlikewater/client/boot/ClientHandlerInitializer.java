package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ClientHeartBeatHandler;
import com.github.fishlikewater.client.handle.ProxyHttpMessageHandler;
import com.github.fishlikewater.client.handle.ProxyTcpMessageHandler;
import com.github.fishlikewater.client.handle.TcpClientHeartBeatHandler;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.MessageProbuf;
import com.github.fishlikewater.kit.ProtocolKit;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @date: 2018年12月25日 15:05
 **/
public class ClientHandlerInitializer extends ChannelInitializer<Channel> {

    private final ProxyConfig proxyConfig;

    private final ProxyType proxyType;
    private final ProxyClient client;


    public ClientHandlerInitializer(ProxyConfig proxyConfig, ProxyType proxyType, ProxyClient client) {
        this.proxyConfig = proxyConfig;
        this.proxyType = proxyType;
        this.client = client;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        /* 是否打开日志*/
        if (proxyConfig != null && proxyConfig.isLogging()) {
            pipeline.addLast(new LoggingHandler());
        }
        if (proxyType == ProxyType.http) {
            assert proxyConfig != null;
            pipeline
                    .addLast(new ProtobufVarint32FrameDecoder())
                    .addLast(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()))
                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                    .addLast(new ProtobufEncoder())
                    .addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS))
                    .addLast(new ClientHeartBeatHandler())
                    .addLast(new ProxyHttpMessageHandler(client));
        }
        if (proxyType ==  ProxyType.proxy_client){
            assert proxyConfig != null;
            pipeline
                    .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                    .addLast(new MyByteToMessageCodec())
                    .addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS))
                    .addLast(new TcpClientHeartBeatHandler())
                    .addLast(new ProxyTcpMessageHandler(client));
        }

    }
}
