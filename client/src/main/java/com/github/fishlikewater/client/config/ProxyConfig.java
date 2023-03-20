package com.github.fishlikewater.client.config;

import com.github.fishlikewater.config.BootModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    /** token(http 及内网穿透代理)*/
    private String token;

    /** 心跳检测间隔*/
    private long timeout = 30L;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    private String proxyPath;

    /** 固定ip 需要以服务端设置前缀为前缀*/
    private String fixedIp;

    private boolean openSocks5;

    /** 启动模式*/
    private BootModel bootModel = BootModel.ONE_TO_ONE;


}

