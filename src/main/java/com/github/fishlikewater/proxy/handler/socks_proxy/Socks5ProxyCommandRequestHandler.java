package com.github.fishlikewater.proxy.handler.socks_proxy;

import com.github.fishlikewater.proxy.handler.proxy_server.CacheUtil;
import com.github.fishlikewater.proxy.kit.ChannelGroupKit;
import com.github.fishlikewater.proxy.kit.IdUtil;
import com.github.fishlikewater.proxy.kit.IpCacheKit;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class Socks5ProxyCommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final boolean b = ChannelGroupKit.removeChannel(ctx.channel());
        if (!b) {
            IpCacheKit.remove(ctx.channel());
        }
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
		log.info("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        //判断是客户端 还是使用端
        Channel channel;
        channel = ChannelGroupKit.find(ctx.channel().id());
        if (channel != null){
            final Channel clientChannel = IpCacheKit.getIpsMap().get(msg.dstAddr());
            if (Objects.isNull(clientChannel)){
                log.info("没有目标地址");
            }else {
                if(msg.type().equals(Socks5CommandType.CONNECT)) {
                    MessageProbuf.SocksMsg.Builder builder = MessageProbuf.SocksMsg.newBuilder();
                    builder.setType(1).setDstAddr(msg.dstAddr()).setDstPort(msg.dstPort());
                    String requestId = IdUtil.next();
                    channel.writeAndFlush(MessageProbuf.Message.newBuilder()
                            .setType(MessageProbuf.MessageType.REQUEST)
                            .setSocksMsg(builder.build())
                            .setRequestId(requestId)).addListener((f)->{
                        if(f.isSuccess()){
                            CacheUtil.put(requestId, ctx.channel(), 10);
                            Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                            ctx.writeAndFlush(commandResponse);
                        }else {
                            log.info("转送失败");
                        }
                    });
                }else {
                    log.debug("连接目标服务器失败");
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    ctx.writeAndFlush(commandResponse);
                }

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

}
