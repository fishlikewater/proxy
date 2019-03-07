package com.github.fishlikewater.proxy.handler.dns;

import com.github.fishlikewater.proxy.kit.MapCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;

import java.net.InetAddress;


/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName DnsServerHandler
 * @Description
 * @Date 2019年03月06日 16:49
 * @since
 **/
@Slf4j
public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private MapCache dnsCache = MapCache.single();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg){

        try {
            // 读取收到的数据
            ByteBuf buf = (ByteBuf) msg.copy().content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            Message indata = new Message(req);
            Record question = indata.getQuestion();
            String domain = indata.getQuestion().getName().toString();
            Object cache = dnsCache.get(domain);
            InetAddress answerIpAddr = null;
            if(cache != null){
                answerIpAddr = (InetAddress)cache;
            }else {
                //解析域名
                answerIpAddr = Address.getByName(domain);
                if(answerIpAddr.getHostName() != null){
                    dnsCache.set(domain, answerIpAddr, 60*60*24);
                }
            }
            Message outdata = (Message)indata.clone();
            //由于接收到的请求为A类型，因此应答也为ARecord。查看Record类的继承，发现还有AAAARecord(ipv6)，CNAMERecord等
            Record answer = new ARecord(question.getName(), question.getDClass(), 64, answerIpAddr);
            outdata.addRecord(answer, Section.ANSWER);
            //发送消息给客户端
            byte[] buf2 = outdata.toWire();
            DatagramPacket response = new DatagramPacket(Unpooled.copiedBuffer(buf2), msg.sender());
            ctx.writeAndFlush(response).addListeners(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    ReferenceCountUtil.release(buf);
                }
            });
        }catch (Exception e){
            log.error("解析域名错误", e.getMessage());
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("解析错误", cause.getMessage());
    }
}
