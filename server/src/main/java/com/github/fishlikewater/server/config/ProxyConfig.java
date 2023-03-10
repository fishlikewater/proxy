package com.github.fishlikewater.server.config;

import com.github.fishlikewater.config.ProxyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {

    private int next = 0;

    /** 服务启动列表*/
    private ProxyType[] type;

    /** 监控地址*/
    private String address;

    /** 监控端口*/
    private int port;

    /** token */
    private String token;

    /** 心跳检测间隔*/
    private long timeout;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 是否开启验证*/
    private boolean auth;

    /** 代理路劲*/
    private String proxyPath;


    private int[] localPorts;

    /** 是否固定本地链接远程使用的端口*/
    private boolean useLocalPorts;


    public int getOneLocalPort(){
        if (Objects.isNull(localPorts)){
            return 0;
        }
        if (next >= localPorts.length){
            next = 0;
        }
        int port = localPorts[next];
        next++;
        return port;
    }
}

