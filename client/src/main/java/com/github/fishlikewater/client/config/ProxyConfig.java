package com.github.fishlikewater.client.config;

import com.github.fishlikewater.client.boot.ProxyClient;
import com.github.fishlikewater.config.ProxyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhangx
 * @version V1.0
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

    /** token(http 及内网穿透代理)*/
    private String token;

    /** 心跳检测间隔*/
    private long timeout = 30L;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 内网穿透代理路劲*/

    private List<HttpMapping> httpMappings;

    private String proxyPath;

    private ProxyType proxyType;

    @Data
    public static class HttpMapping{

        private String address;

        private int port;

        private boolean longConnection;

        private String name;

    }

}

