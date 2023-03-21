package com.github.fishlikewater.cutpackets;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.TCPPacket;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
        /*-------第一步,显示网络设备列表-------- */
        // 获取网络接口列表，返回你所有的网络设备数组,一般就是网卡;
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        int k = -1;
        // 显示所有网络设备的名称和描述信息;
        // 要注意的是,显示出来的网络设备在不同网络环境下是不同的,可以在控制台使用 ipconfig /all命令查看;
        for (NetworkInterface n : devices) {
            k++;
            System.out.println("序号 " + k + "   " + n.name + "     |     " + n.description);
            System.out.println("------------------------------------------------");
        }
        //第二步,监听选中的网卡;
        try {
            // 参数一:选择一个网卡，调用 JpcapCaptor.openDevice()连接，返回一个 JpcapCaptor类的对象 jpcap;
            // 参数二:限制每一次收到一个数据包，只提取该数据包中前1512个字节;
            // 参数三:设置为非混杂模式,才可以使用下面的捕获过滤器方法;
            // 参数四:指定超时的时间;

            JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[3], 2000, false, 10000);

            //第三步,捕获数据包;
            // 调用 processPacket()方法, count = -1对该方法无影响,主要受 to_ms控制,改成其他数值则会控制每一次捕获包的数目;
            // 换而言之,影响 processPacket()方法的因素有且只有两个,分别是count 和 to_ms;
            // 抓到的包将调用这个 new Receiver()对象中的 receivePacket(Packet packet)方法处理；
            jpcap.setFilter("tcp and dst net 192.168.12.0/24", true);
            jpcap.loopPacket(-1, packet -> {
                final TCPPacket tcpPacket = (TCPPacket) packet;
                System.out.println(tcpPacket.dst_ip);
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("抓取数据包时出现异常!!");
        }
    }

}
