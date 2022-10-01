package com.github.fishlikewater.proxy.boot;

import com.github.fishlikewater.proxy.gui.ConnectionUtils;
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

    private TcpProxyClient client;

    public ConnectionListener(TcpProxyClient client){
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            log.info("connect fail, will reconnect");
            ConnectionUtils.setStateText("connect fail, will reconnect");
            //TcpProxyClient.build().retryOne();
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    client.start();
                }
            }, 30, TimeUnit.SECONDS);

        }
    }
}
