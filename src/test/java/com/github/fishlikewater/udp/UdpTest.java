package com.github.fishlikewater.udp;

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
        new UdpClient().sendPackage();
    }
}
