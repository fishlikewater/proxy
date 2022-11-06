package com.github.fishlikewater.proxyp2p.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月29日 14:34
 **/
@Data
@Component
@ConfigurationProperties("proxy.call")
public class CallConfig {

    private String address;

    private int port;

    private String serverAddress;

    private int serverPort;

    private long timeout = 30;

    private String name;

    private int socksProt;

    private String socksAddress;

    /** 是否开启验证*/
    private boolean auth;

}
