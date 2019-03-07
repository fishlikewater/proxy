package com.github.fishlikewater.proxy.handler.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
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
public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // 读取收到的数据
        ByteBuf buf = (ByteBuf) msg.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        Message indata = new Message(req);
        System.out.println("\nindata = " + indata.toString());
        Record question = indata.getQuestion();
        System.out.println("question = " + question);
        String domain = indata.getQuestion().getName().toString();
        System.out.println("domain = " + domain);
        //解析域名
        InetAddress answerIpAddr = Address.getByName(domain);
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
    }
}
