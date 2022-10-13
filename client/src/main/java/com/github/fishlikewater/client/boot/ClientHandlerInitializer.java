package com.github.fishlikewater.client.boot;


import com.github.fishlikewater.client.config.ProxyConfig;
import com.github.fishlikewater.client.handle.ClientHeartBeatHandler;
import com.github.fishlikewater.client.handle.ClientMessageHandler;
import com.github.fishlikewater.client.handle.TcpClientHandler;
import com.github.fishlikewater.codec.ByteArrayCodec;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
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

    private final ProxyType proxyType;

    private ProxyClient client;

    public ClientHandlerInitializer(ProxyConfig proxyConfig, ProxyClient client, ProxyType proxyType) {
        this.proxyConfig = proxyConfig;
        this.client = client;
        this.proxyType = proxyType;
    }
    public ClientHandlerInitializer(ProxyType proxyType){
        this.proxyType = proxyType;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        /* 是否打开日志*/
        if (proxyConfig.isLogging()) {
            pipeline.addLast(new LoggingHandler());
        }
        if (proxyType == ProxyType.proxy_client) {
            pipeline
                    .addLast(new ProtobufVarint32FrameDecoder())
                    .addLast(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()))
                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                    .addLast(new ProtobufEncoder())
                    .addLast(new IdleStateHandler(0, 0, proxyConfig.getTimeout(), TimeUnit.SECONDS))
                    .addLast(new ClientHeartBeatHandler(client))
                    .addLast(new ClientMessageHandler(client));
        } /* tcp代理*/
        else if (proxyType == ProxyType.tcp_client){
            pipeline.addLast(new ByteArrayCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new TcpClientHandler(proxyConfig.getProxyPath()));
        }

    }
}
