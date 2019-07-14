package com.github.fishlikewater.proxy.kit;

import lombok.Data;

import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 16:36
 * @since
 **/
@Data
public class Request {

    private String httpVersion;

    private String method;

    private String url;

    private byte[] body;

    private  Map<String, String> header;
}
