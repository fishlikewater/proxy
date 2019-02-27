package com.github.fishlikewater.proxy;

import com.github.fishlikewater.proxy.kit.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ConnectionTest
 * @Description
 * @Date 2019年02月27日 15:37
 * @since
 **/
public class ConnectionTest {

    @Test
    public void test1() throws InterruptedException {
        Bootstrap b = new Bootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-boss@"));
        b.group(bossGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress("www.baidu.com", 443)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new SimpleChannelInboundHandler() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println("111");
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            System.out.println("11");
                        } else {

                        }
                    }
                }).sync();
    }
}
