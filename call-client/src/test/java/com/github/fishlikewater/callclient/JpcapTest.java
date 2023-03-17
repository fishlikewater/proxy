package com.github.fishlikewater.callclient;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月14日 16:50
 **/
public class JpcapTest {


    @Test
    public void testJp(){
        StringBuilder errsb = new StringBuilder();
        List<PcapIf> devs = new ArrayList<PcapIf>();
        int r = Pcap.findAllDevs(devs, errsb);
        if (r == Pcap.NOT_OK || devs.isEmpty()) {
            System.err.println("未获取到网卡");
        } else {
            System.out.println("获取到网卡：");
            System.out.println(devs);
        }
    }

}
