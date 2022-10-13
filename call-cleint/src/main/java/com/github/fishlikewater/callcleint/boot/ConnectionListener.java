package com.github.fishlikewater.callcleint.boot;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ConnectionListener
 * @Description
 * @date 2018年12月25日 17:17
 **/
@Slf4j
public class ConnectionListener implements ChannelFutureListener {

    private final ProxyClient client;

    public ConnectionListener(ProxyClient client){
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        if (!channelFuture.isSuccess()) {
            log.info("connect fail, will reconnect");
            //ProxyClient.build().retryOne();
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(client::start, 30, TimeUnit.SECONDS);

        }
    }
}
