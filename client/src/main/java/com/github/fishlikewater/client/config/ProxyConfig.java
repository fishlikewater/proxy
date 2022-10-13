package com.github.fishlikewater.client.config;

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

    /** 监控地址*/
    private String address;

    /** 监控端口*/
    private int port;

    /** 验证密码*/
    private String password;

    /** token(http 及内网穿透代理)*/
    private String token;

    /** 心跳检测间隔*/
    private long timeout;

    /** 是否使用netty日志处理器*/
    private boolean logging;

    /** 内网穿透代理路劲*/
    private String proxyPath;

    /** 客户端本地 映射端口*/
    private int localPort = 8081;

    /** 客户端 http 映射地址*/
    private String localAddress = "127.0.0.1";

    /** 打开内存泄漏检测*/
    private boolean isOpenCheckMemoryLeak = false;

    /** 是否开启全局流量限制*/
    private boolean isOpenGlobalTrafficLimit = false;

    /** 写限制*/
    private long writeLimit = 256*1024;

    /** 读限制*/
    private long readLimit = 256*1024;

    /** 客户端类型 0-> 访问对象 客户端  1->发起访问 客户端*/
    private int clientType = 0;

    public boolean getIsOpenCheckMemoryLeak() {
        return isOpenCheckMemoryLeak;
    }

    public ProxyConfig setIsOpenCheckMemoryLeak(boolean openCheckMemoryLeak) {
        isOpenCheckMemoryLeak = openCheckMemoryLeak;
        return this;
    }

    public boolean getIsOpenGlobalTrafficLimit() {
        return isOpenGlobalTrafficLimit;
    }

    public ProxyConfig setIsOpenGlobalTrafficLimit(boolean openGlobalTrafficLimit) {
        isOpenGlobalTrafficLimit = openGlobalTrafficLimit;
        return this;
    }
}

