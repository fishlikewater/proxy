package com.github.fishlikewater.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {

    /** 服务器地址*/
    private String address;

    /** 服务器端口*/
    private int port;

    /** 连接服务器的验证*/
    private String token;

    /** 心跳检测间隔*/
    private long timeout = 30L;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 类vpn模式下 固定ip 需要以服务端设置前缀为前缀*/
    private String fixedIp;

    /** 直连ip 配置该值后 对应ip主机相当与本机的影子，可以把请求全部转发到改ip主机，代替本机发起请求*/
    private String linkIp;

    /** 是否开启socks5服务(当只作为被调用的资源注册时 不需要开启socks5服务)*/
    private boolean openSocks5;

    /** 每一帧最大字节 */
    private DataSize maxFrameLength = DataSize.ofBytes(5*1024 * 1024);

    /** 客户端开放端口列表,若为空 则不限制*/
    private int[] localPorts;

    private Map<Integer, Mapping> mappingMap = new HashMap<>();


    @Data
    public static class Mapping{

        /**映射ip*/
        private String mappingIp;

        /** 映射端口*/
        private int mappingPort;

        /** ssl*/
        private boolean ssl = false;

    }


}

