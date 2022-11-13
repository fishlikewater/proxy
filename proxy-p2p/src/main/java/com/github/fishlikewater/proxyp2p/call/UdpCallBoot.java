package com.github.fishlikewater.proxyp2p.call;

import com.github.fishlikewater.kit.EpollKit;
import com.github.fishlikewater.kit.NamedThreadFactory;
import com.github.fishlikewater.proxyp2p.call.handle.CallHeartBeatHandler;
import com.github.fishlikewater.proxyp2p.call.handle.CallUdpP2pDataHandler;
import com.github.fishlikewater.proxyp2p.config.CallConfig;
import com.github.fishlikewater.proxyp2p.kit.BootStrapFactroy;
import com.github.fishlikewater.proxyp2p.kit.MessageKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月31日 12:52
 **/
@Slf4j
public class UdpCallBoot {

    private EventLoopGroup bossGroup;

    private final CallConfig callConfig;

    public UdpCallBoot(CallConfig callConfig){
        this.callConfig = callConfig;
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
                        pipeline.addLast(new IdleStateHandler(0, 0, callConfig.getTimeout(), TimeUnit.SECONDS))
                                .addLast(new CallHeartBeatHandler(callConfig));
                        pipeline.addLast(new CallUdpP2pDataHandler(callConfig));
                    }
                });
        CallHeartBeatHandler.setInetSocketAddress(new InetSocketAddress(callConfig.getServerAddress(), callConfig.getServerPort()));
        final ChannelFuture channelFuture = b.bind(0).addListener(future -> {}).sync();
        if (channelFuture.isSuccess()){
            CallKit.setChannel(channelFuture.channel());
        }
    }


    /**
     * 关闭服务
     */
    public void stop() {
        log.info("⬢ call shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            log.info("⬢ shutdown call successful");
        } catch (Exception e) {
            log.error("⬢ call shutdown error", e);
        }
    }
}
