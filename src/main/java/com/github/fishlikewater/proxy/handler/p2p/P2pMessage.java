package com.github.fishlikewater.proxy.handler.p2p;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月23日 9:11
 * @since
 **/
@Data
public class P2pMessage implements Serializable {

    private String targetIp;

    private int targetPort;

    private String body;

}
