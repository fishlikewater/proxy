package com.github.fishlikewater.client.handle;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.config.BootModel;
import com.github.fishlikewater.socks5.handle.Socks5Kit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月19日 13:03
 **/
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private final ProxyClient client;

    public ClientMessageHandler(ProxyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(client::start, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws InterruptedException {
        final MessageProtocol.CmdEnum cmd = msg.getCmd();
        switch (cmd) {
            case AUTH:
                HandleKit.toRegister(msg, ctx, client.getProxyConfig());
                break;
            case REGISTER:
                if (client.getProxyConfig().getBootModel() == BootModel.VPN && msg.getState() == 1){
                    log.info("本机分配的虚拟ip为: " + new String(msg.getBytes(), StandardCharsets.UTF_8));
                }else if (client.getProxyConfig().getBootModel() == BootModel.VPN && msg.getState() == 0){
                    final EventLoop loop = ctx.channel().eventLoop();
                    loop.schedule(() -> HandleKit.toRegister(msg, ctx, client.getProxyConfig())
                    , 30, TimeUnit.SECONDS);
                }
                else {
                    log.info(new String(msg.getBytes(), StandardCharsets.UTF_8));
                }
                break;
            case DATA_CHANNEL:
                HandleKit.createDataChannel(ctx, client.getProxyConfig(), new String(msg.getBytes(), StandardCharsets.UTF_8));
                break;
            case HEALTH:
                log.debug("get health info");
                break;
            case REQUEST:
                HandleKit.handlerRequest(msg, ctx, client.getProxyConfig());
                break;
            case RESPONSE:
                final Channel socksChannel = ctx.channel().attr(Socks5Kit.CHANNELS_SOCKS).get().get(msg.getId());
                if (Objects.nonNull(socksChannel) && socksChannel.isActive()) {
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(msg.getBytes().length);
                    buf.writeBytes(msg.getBytes());
                    socksChannel.writeAndFlush(buf);
                }
                break;
            case CONNECTION:
                HandleKit.handlerConnection2(msg, ctx, client.getProxyConfig());
                break;
            case ACK:
                final Channel socksChannel1 = ctx.channel().attr(Socks5Kit.CHANNELS_SOCKS).get().get(msg.getId());
                if (Objects.nonNull(socksChannel1) && socksChannel1.isActive()) {
                    if (msg.getState() == 1)
                    {
                        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                        socksChannel1.writeAndFlush(commandResponse);
                    }
                    if (msg.getState() == 0)
                    {
                        Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                        socksChannel1.writeAndFlush(commandResponse);
                    }
                }
                break;
            default:
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            log.error("happen error: ", cause);
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
