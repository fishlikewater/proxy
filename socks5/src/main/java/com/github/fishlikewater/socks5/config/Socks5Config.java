package com.github.fishlikewater.socks5.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年03月05日 15:55
 **/
@ConfigurationProperties("socks")
@Data
@Component
public class Socks5Config {

    private int port;

    private String address;

    /** 验证用户名(客户端用户名) */
    private String username;

    /** 验证密码*/
    private String password;

    /** 是否开启验证*/
    private boolean auth;

}
