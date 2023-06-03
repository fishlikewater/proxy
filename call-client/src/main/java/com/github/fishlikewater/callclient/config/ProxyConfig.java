package com.github.fishlikewater.callclient.config;

import com.github.fishlikewater.config.ProxyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {


    private ProxyType proxyType;

    /** 服务器地址*/
    private String address;

    /** 服务器端口*/
    private int port;

    /** token */
    private String token;

    /** 心跳检测间隔*/
    private long timeout;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 匹配目标机*/
    private String proxyPath;

    private boolean mapping;

    /** 每一帧最大字节 */
    private DataSize maxFrameLength = DataSize.ofBytes(5*1024 * 1024);

    private List<ProxyMapping> proxyMappings;


    @Data
    public static class ProxyMapping{

        private String domain;

        private String ip;

    }

}

