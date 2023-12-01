package com.github.fishlikewater.server.kit;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
public interface ConnectionValidate {

    boolean validate(String token, String verify);
}
