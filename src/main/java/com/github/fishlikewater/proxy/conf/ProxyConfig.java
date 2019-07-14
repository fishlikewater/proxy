package com.github.fishlikewater.proxy.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ProxyConfig
 * @Description
 * @Date 2019年03月05日 15:55
 * @since
 **/
@ConfigurationProperties("proxy")
@Data
@Component
public class ProxyConfig {

    /** 代理类型*/
    private ProxyType type;

    /** 监控地址*/
    private String address;

    /** 监控端口*/
    private int port;

    /** 服务端 http开放端口*/
    private int httpPort;

    /** 验证用户名 */
    private String username;

    /** 验证密码*/
    private String password;

    /** token*/
    private String token;

    /** 心跳检测间隔*/
    private long timeout;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 是否开启验证*/
    private boolean auth;

    private String proxyDns;

    private String proxyPath;

    /** 客户端 http 映射端口*/
    private int localPort = 8081;

    /** 客户端 http 映射端口*/
    private String localAddress = "127.0.0.1";

    /** 打开内存泄漏检测*/
    private boolean isOpenCheckMemoryLeak = false;

    /** 是否开启全局流量限制*/
    private boolean isOpenGlobalTrafficLimit = false;

    /** 写限制*/
    private long writeLimit = 1024*1024;

    /** 读限制*/
    private long readLimit = 1024*1024;
}

