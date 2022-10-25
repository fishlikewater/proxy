package com.github.fishlikewater.callcleint.config;

import com.github.fishlikewater.config.ProxyType;
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


    private ProxyType proxyType;

    /** 服务器地址*/
    private String address;

    /** 服务器端口*/
    private int port;

    private int socksProt;

    private String socksAddress;

    /** token */
    private String token;

    /** 心跳检测间隔*/
    private long timeout;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 匹配目标机*/
    private String proxyPath;

    /** 验证用户名(客户端用户名) */
    private String username;

    /** 验证密码*/
    private String password;

    /** 是否开启验证*/
    private boolean auth;

}

