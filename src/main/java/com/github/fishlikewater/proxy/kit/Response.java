package com.github.fishlikewater.proxy.kit;

import lombok.Data;

import java.util.Map;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月13日 17:57
 * @since
 **/
@Data
public class Response {

    private int code;

    private byte[] body;

    private Map<String, String> header;

}
