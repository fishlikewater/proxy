package com.github.fishlikewater.callcleint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyConfig
 * @Description
 * @since: 2019年03月05日 15:55
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {

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

    /** 内网穿透代理路劲 匹配目标机*/
    private String proxyPath;

    /** 映射*/
    private Mapping[] mappings = new Mapping[]{};


    /** 客户端类型 0-> 访问对象 客户端  1->发起访问 客户端*/
    private int clientType = 0;


    @Data
    public static class Mapping{

        private String localAddress;

        private int localPort;

        private String protocol;

        private String remoteAddress;

        private int remotePort;


    }
}

