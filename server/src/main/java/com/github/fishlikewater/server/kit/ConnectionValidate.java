package com.github.fishlikewater.server.kit;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName ConnectionValidate
 * @Description
 * @date 2018年11月25日 0:07
 **/
public interface ConnectionValidate {

    boolean validate(String token, String verify);
}
