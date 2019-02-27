package com.github.fishlikewater.proxy.kit;

import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName PassWordCheck
 * @Description
 * @Date 2019年02月27日 12:51
 * @since
 **/
@Component
@Slf4j
public class PassWordCheck {

    private static String username;
    private static String password;

    @Value("${proxy.username}")
    public void setUsername(String uname){
        username = uname;
    }
    @Value("${proxy.password}")
    public void setPassword(String pw){
        password = pw;
    }

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
            if(split1[0].equals(username) && split1[1].equals(password)){
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
