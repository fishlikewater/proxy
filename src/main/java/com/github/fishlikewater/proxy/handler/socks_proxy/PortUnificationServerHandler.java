package com.github.fishlikewater.proxy.handler.socks_proxy;

import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.handler.proxy_server.DefaultConnectionValidate;
import com.github.fishlikewater.proxy.handler.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.proxy.handler.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.proxy.handler.socks.Socks5PasswordAuthRequestHandler;
import com.github.fishlikewater.proxy.kit.MessageProbuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v4.Socks4ServerDecoder;
import io.netty.handler.codec.socksx.v4.Socks4ServerEncoder;
import io.netty.handler.codec.socksx.v5.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Description:
 * @date: 2022年08月02日 9:10
 * @author: fishlikewater@126.com
 * @version: V1.0.0
 * @since:
 **/
@Slf4j
@RequiredArgsConstructor
public class PortUnificationServerHandler extends ByteToMessageDecoder {

    private final ProxyConfig proxyConfig;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        final int readerIndex = in.readerIndex();
        if (in.writerIndex() == readerIndex) {
            return;
        }
        ChannelPipeline p = ctx.pipeline();
        final byte versionVal = in.getByte(readerIndex);
        try {
            SocksVersion version = SocksVersion.valueOf(versionVal);
            if (version == SocksVersion.SOCKS5){

            }else if(version == SocksVersion.UNKNOWN){
                if (p.get(Socks5CommandRequestDecoder.class) != null){
                    p.remove(Socks5CommandRequestDecoder.class);
                }
                if (p.get(Socks5PasswordAuthRequestHandler.class) != null){
                    p.remove(Socks5PasswordAuthRequestHandler.class);
                }
                if (p.get(Socks5PasswordAuthRequestDecoder.class) != null){
                    p.remove(Socks5PasswordAuthRequestDecoder.class);
                }
                if (p.get(Socks5InitialAuthHandler.class) != null){
                    p.remove(Socks5InitialAuthHandler.class);
                }
                if (p.get(Socks5ServerEncoder.class) != null){
                    p.remove(Socks5ServerEncoder.class);
                }
                if (p.get(Socks5InitialRequestDecoder.class) != null){
                    p.remove(Socks5InitialRequestDecoder.class);
                }
                if (p.get(Socks5ProxyCommandRequestHandler.class) != null){
                    p.remove(Socks5ProxyCommandRequestHandler.class);
                }

                p.addFirst(new ProtobufEncoder());
                p.addFirst(new ProtobufVarint32LengthFieldPrepender());
                p.addFirst(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()));
                p.addFirst(new ProtobufVarint32FrameDecoder());
                p.addLast("proxyProtobufServerHandler", new Dest2ClientHandler(new DefaultConnectionValidate(), proxyConfig));
            }else {
                log.error("只支持SOCKS5");
            }
        }catch (Exception e){
            p.addFirst(new ProtobufEncoder());
            p.addFirst(new ProtobufVarint32LengthFieldPrepender());
            p.addFirst(new ProtobufDecoder(MessageProbuf.Message.getDefaultInstance()));
            p.addFirst(new ProtobufVarint32FrameDecoder());
            p.addLast("proxyProtobufServerHandler", new Dest2ClientHandler(new DefaultConnectionValidate(), proxyConfig));
        }
        in.resetReaderIndex();
        p.remove(this);
    }
}
