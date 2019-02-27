package com.github.fishlikewater.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyServiceInitializer
 * @Description
 * @date 2019年02月26日 21:47
 **/
public class ProxyServiceInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        p.addLast("httpcode", new HttpServerCodec());
        p.addLast("httpservice", new HttpServiceHandler());
    }
}
