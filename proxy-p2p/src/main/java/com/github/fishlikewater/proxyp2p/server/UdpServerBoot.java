package com.github.fishlikewater.proxyp2p.server;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.proxyp2p.config.ServerConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import com.github.fishlikewater.proxyp2p.server.handle.UdpP2pDataHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月29日 14:28
 **/
public class UdpServerBoot {

    private final ServerConfig serverConfig;

    public UdpServerBoot(ServerConfig serverConfig){
        this.serverConfig = serverConfig;
    }

    public void start() throws InterruptedException {
        final Bootstrap b = BootStrapFactroy.bootstrapConfig();
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            b.group(new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"))).channel(EpollDatagramChannel.class);
        } else {
            b.group(new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"))).channel(NioDatagramChannel.class);
        }
        b.option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        final ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("udpDecoder", new DatagramPacketDecoder(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance())));
                        pipeline.addLast("udpEncoder", new DatagramPacketEncoder<>(new ProtobufEncoder()));
                        pipeline.addLast(new UdpP2pDataHandler());
                    }
                });
        b.bind(serverConfig.getAddress(), serverConfig.getPort()).sync().channel().closeFuture().await();
    }

}
