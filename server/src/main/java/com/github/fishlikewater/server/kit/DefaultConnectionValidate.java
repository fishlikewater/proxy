package com.github.fishlikewater.server.kit;

import cn.hutool.core.util.StrUtil;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
public class DefaultConnectionValidate implements ConnectionValidate {
    @Override
    public boolean validate(String token, String verify) {
        if(StrUtil.isEmpty(verify)){
            return true;
        }else {
            return verify.equals(token);
        }
    }
}
