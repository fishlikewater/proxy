package com.github.fishlikewater.server.kit;

import io.netty.handler.codec.http.HttpRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年02月27日 12:51
 **/
@Slf4j
public class PassWordCheck {

    @Setter
    private static String username;
    @Setter
    private static String password;


    //basic方式登录
    public static boolean basicLogin(HttpRequest req) {
       if(username == null || password == null){
           return true;
       }
        //获取请求头中的 Proxy-Authorization
        String s = req.headers().get("Proxy-Authorization");
        if (s == null) {
            return false;
        }
        //密码的形式是   `Basic 帐号:密码`用冒号拼接在一起，在取base64
        try {
            String[] split = s.split(" ");
            byte[] decode = Base64.getDecoder().decode(split[1]); //去数组中的第二个，第一个是一个Basic固定的字符
            String userNamePassWord = new String(decode);
            String[] split1 = userNamePassWord.split(":", 2);
            return split1[0].equals(username) && split1[1].equals(password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
