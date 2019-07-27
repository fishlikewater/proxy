package com.github.fishlikewater.udp;

import com.alibaba.fastjson.JSON;
import com.github.fishlikewater.proxy.handler.p2p.P2pMessage;
import io.netty.util.CharsetUtil;
import org.junit.Test;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName UdpTest
 * @Description
 * @Date 2019年03月07日 9:54
 * @since
 **/
public class UdpTest {

    @Test
    public void testUdp(){
        //new UdpClient().sendPackage();
        P2pMessage msg = new P2pMessage();
        msg.setBody("1111");
        msg.setTargetIp("192.168.0.117");
        msg.setTargetPort(13455);
        byte[] content = JSON.toJSONString(msg).getBytes(CharsetUtil.UTF_8);
        int length =content.length;
        System.out.println(length);
    }
}
