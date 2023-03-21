package com.github.fishlikewater.cutpackets;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.TCPPacket;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.Scanner;

/**
 * <p>
 *  网卡数据包拦截
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月21日 10:28
 **/
public class CutPacketsHelper {

    public void startCutPackets(String ipPrefix, Channel channel){
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        int k = 0;
        for (NetworkInterface n : devices) {
            System.out.println("序号: " + k + n.name + "     |     " + n.description);
            System.out.println("------------------------------------------------");
            k++;
        }
        int selectDevice = 0;
        do {
            System.out.println("请根据序号选择拦截的网卡: ");
            final Scanner scanner = new Scanner(System.in);
            selectDevice = scanner.nextInt();
        } while (selectDevice < 0 || selectDevice > devices.length);
        try {
            JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[3], 2000, false, 10000);
            jpcap.setFilter("tcp and dst net " + ipPrefix + "0/24", true);
            jpcap.loopPacket(-1, packet -> {
                if (packet instanceof TCPPacket){
                    final TCPPacket tcpPacket = (TCPPacket) packet;
                    final String hostAddress = tcpPacket.src_ip.getHostAddress();
                    System.out.println(hostAddress);
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("抓取数据包时出现异常!!");
        }


    }


}
