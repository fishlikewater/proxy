package com.github.fishlikewater.proxy.handler.proxy_server;

import org.springframework.util.StringUtils;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName DefaultConnectionValidate
 * @Description
 * @date 2018年11月25日 11:34
 **/
public class DefaultConnectionValidate implements ConnectionValidate {
    @Override
    public boolean validate(String token, String verify) {
        if(StringUtils.isEmpty(verify)){
            return true;
        }else {
            if(verify.equals(token)){
                return true;
            }
        }
        return false;
    }
}
