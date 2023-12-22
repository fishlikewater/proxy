package com.github.fishlikewater.cutpackets;

import com.github.fishlikewater.codec.MessageProtocol;
import com.github.fishlikewater.kit.IdUtil;
import io.netty.channel.Channel;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.TCPPacket;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 网卡数据包拦截
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月21日 10:28
 **/
public class CutPacketsHelper {

    private JpcapSender jpcapSender;

    private final Map<String, Long> map = new ConcurrentHashMap<>(100);

    public void startCutPackets(String ipPrefix, Channel channel) {
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
            jpcapSender = jpcap.getJpcapSenderInstance();
            jpcap.setFilter("tcp and dst net " + ipPrefix + "0/24", true);
            jpcap.loopPacket(-1, packet -> {
                if (packet instanceof TCPPacket) {
                    final TCPPacket tcpPacket = (TCPPacket) packet;
                    final boolean syn = tcpPacket.syn;
                    final boolean ack = tcpPacket.ack;
                    final String srcIp = tcpPacket.src_ip.getHostAddress();
                    final int srcPort = tcpPacket.src_port;
                    String src = srcIp + ":" + srcPort;
                    final String dstIp = tcpPacket.dst_ip.getHostAddress();
                    final int dstPort = tcpPacket.dst_port;
                    String dst = dstIp + ":" + dstPort;
                    long requestId = 0;
                    if (syn && !ack) {
                        requestId = IdUtil.id();
                        map.put(src + "_" + dst, requestId);
                    } else {
                        requestId = map.get(src + "_" + dst);
                    }
                    if (requestId == 0) {
                        requestId = IdUtil.id();
                        map.put(src + "_" + dst, requestId);
                    }
                    final byte[] data = tcpPacket.data;
                    final MessageProtocol.Dst dst1 = new MessageProtocol.Dst().setDstAddress(dstIp).setDstPort(dstPort);
                    final MessageProtocol message = new MessageProtocol();
                    message
                            .setId(requestId)
                            .setDst(dst1)
                            .setCmd(MessageProtocol.CmdEnum.REQUEST)
                            .setProtocol(MessageProtocol.ProtocolEnum.SOCKS)
                            .setBytes(data);
                    channel.writeAndFlush(message);
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("抓取数据包时出现异常!!");
        }
    }


    public void send(String srcIp, int srcPort, byte[] data) {
        //final TCPPacket packet = new TCPPacket(srcIp, srcPort);
        //jpcapSender.sendPacket(packet);
    }


}
