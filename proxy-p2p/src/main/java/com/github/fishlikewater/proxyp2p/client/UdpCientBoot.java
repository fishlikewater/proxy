package com.github.fishlikewater.proxyp2p.client;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.proxyp2p.client.handle.UdpP2pDataHandler;
import com.github.fishlikewater.proxyp2p.codec.MyDatagramPacketDecoder;
import com.github.fishlikewater.proxyp2p.codec.MyProtobufDecoder;
import com.github.fishlikewater.proxyp2p.config.ClientConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageProbuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月29日 14:28
 **/
@Slf4j
public class UdpCientBoot {

    private EventLoopGroup bossGroup;

    private final ClientConfig clientConfig;

    public UdpCientBoot(ClientConfig clientConfig){
        this.clientConfig = clientConfig;
    }

    public void start() throws InterruptedException {
        final Bootstrap b = BootStrapFactroy.bootstrapConfig();
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            bossGroup =new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
            b.group(bossGroup).channel(EpollDatagramChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
            b.group(bossGroup).channel(NioDatagramChannel.class);
        }
        b.option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        final ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst("udpEncoder", new DatagramPacketEncoder<>(new ProtobufEncoder()));
                        pipeline.addFirst("udpDecoder", new MyDatagramPacketDecoder(new MyProtobufDecoder(MessageProbuf.Message.getDefaultInstance())));
                        pipeline.addLast(new UdpP2pDataHandler(clientConfig));
                    }
                });
        b.bind(clientConfig.getPort()).addListener(future -> {
        }).sync();
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ client shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            log.info("⬢ shutdown client successful");
        } catch (Exception e) {
            log.error("⬢ client shutdown error", e);
        }
    }

}
