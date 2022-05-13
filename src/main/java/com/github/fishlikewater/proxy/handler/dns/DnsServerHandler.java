package com.github.fishlikewater.proxy.handler.dns;

import com.github.fishlikewater.proxy.kit.MapCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Record;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


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
public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private final MapCache dnsCache = MapCache.single();
    private static final HashMap<String, byte[]> ipMap = new HashMap<>();

    static {
        InputStream inputStream = DnsServerHandler.class.getResourceAsStream("/filter.list");
        InputStreamReader isr = new InputStreamReader(inputStream);
        try {
            BufferedReader bufferedReader = new BufferedReader(isr);
            String str = "";
            while ((str = bufferedReader.readLine()) != null) {
                String[] split = str.trim().split("/");
                String[] strs = split[1].split("\\.");
                byte[] bytes = new byte[4];
                for (int i = 0; i < strs.length; i++) {
                    bytes[i] = (byte) Integer.parseInt(strs[i]);
                }
                ipMap.put(split[0]+".", bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query){
        DatagramDnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        try {
            DefaultDnsQuestion dnsQuestion = query.recordAt(DnsSection.QUESTION);
            response.addRecord(DnsSection.QUESTION, dnsQuestion);
            Optional<Map.Entry<String, byte[]>> first = ipMap.entrySet().stream().filter(m -> dnsQuestion.name().endsWith(m.getKey())).findFirst();
            ByteBuf buf = null;
            if (first.isPresent()) {
                buf = Unpooled.wrappedBuffer(first.get().getValue());
                DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(), DnsRecordType.A, 60, buf);
                response.addRecord(DnsSection.ANSWER, queryAnswer);
            } else {
                Object cache = dnsCache.get(dnsQuestion.name());
                Record[] recordArr;
                if(cache != null){
                    recordArr = (Record[])cache;
                }else {
                    recordArr = DNSUtils.getRecordArr(dnsQuestion.name());
                    dnsCache.set(dnsQuestion.name(), recordArr, 60*60);
                }
                if(recordArr != null){
                    for (Record record : recordArr) {
                        String string = record.rdataToString();
                        String[] split = string.split("\\.");
                        byte[] bytes = new byte[4];
                        for (int i = 0; i < split.length; i++) {
                            bytes[i] = (byte) Integer.parseInt(split[i]);
                        }
                        buf = Unpooled.wrappedBuffer(bytes);
                        DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(), DnsRecordType.A, 10, buf);
                        response.addRecord(DnsSection.ANSWER, queryAnswer);
                    }
                }
            }
        }catch (Exception e){
            log.error("解析域名错误", e);
        } finally {
            ctx.writeAndFlush(response).addListeners(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                }
            });
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("解析错误", cause.getMessage());
    }
}
