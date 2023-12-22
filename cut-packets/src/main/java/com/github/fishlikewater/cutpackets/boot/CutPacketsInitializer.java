package com.github.fishlikewater.cutpackets.boot;

import com.github.fishlikewater.cutpackets.handler.CutPacketsHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月15日 6:03
 **/
public class CutPacketsInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new CutPacketsHandler());
    }
}
