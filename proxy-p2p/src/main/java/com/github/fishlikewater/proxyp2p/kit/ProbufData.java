package com.github.fishlikewater.proxyp2p.kit;

import com.google.protobuf.MessageLite;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月30日 12:11
 **/
@Data
@AllArgsConstructor
public class ProbufData {

    private InetSocketAddress sender;

    private InetSocketAddress recipient;

    private MessageLite message;
}
