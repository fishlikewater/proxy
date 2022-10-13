package com.github.fishlikewater.callcleint.handle;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyServiceInitializer
 * @Description
 * @date 2019年02月26日 21:47
 **/
public class TempClientServiceInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("http", new HttpClientCodec());
        p.addLast("aggregator", new HttpObjectAggregator(1024*1024*10));
    }
}
