package com.github.fishlikewater.proxy.handler.dns;

import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;

import java.net.UnknownHostException;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年09月06日 9:20
 * @since
 **/
@Slf4j
public class DNSUtils {

    private static Resolver resolver;

    public static void init(String host) {
        try {
            resolver = new SimpleResolver(host);
        } catch (UnknownHostException e) {
            log.error("创建解析dns服务异常");
        }
    }

    public static Record[] getRecordArr(String host) throws TextParseException {
        Lookup lookup = new Lookup(host, Type.A);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        if(lookup.getResult()== Lookup.SUCCESSFUL){
            return lookup.getAnswers();
        }else {
            return null;
        }
    }


}
