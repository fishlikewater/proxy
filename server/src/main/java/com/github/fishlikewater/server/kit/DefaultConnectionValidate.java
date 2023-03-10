package com.github.fishlikewater.server.kit;

import cn.hutool.core.util.StrUtil;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName DefaultConnectionValidate
 * @Description
 * @date 2018年11月25日 11:34
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
