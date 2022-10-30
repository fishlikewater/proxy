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
@ConfigurationProperties("proxy.server")
public class ServerConfig {

    private String address;

    private int port;


}
