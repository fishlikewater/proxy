package com.github.fishlikewater.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName UdpClient
 * @Description
 * @Date 2019年03月06日 16:57
 * @since
 **/
@Slf4j
public class UdpClient {

    public void sendPackage() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ClientHandler());

            Channel ch = b.bind(0).sync().channel();

            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("hello!!!", CharsetUtil.UTF_8),
                    new InetSocketAddress("255.255.255.255", 11080))).sync();

            log.info("Search, sendPackage()");
            // QuoteOfTheMomentClientHandler will close the DatagramChannel when a
            // response is received.  If the channel is not closed within 5 seconds,
            // print an error message and quit.
            // 等待15秒钟
            if (!ch.closeFuture().await(15000)) {
                log.info("Search request timed out.");
            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("Search, An Error Occur ==>" + e);
        }finally {
            group.shutdownGracefully();
        }
    }
}
