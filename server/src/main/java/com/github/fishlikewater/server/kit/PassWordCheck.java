/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.server.kit;

import io.netty.handler.codec.http.HttpRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 * @since 2019年02月27日 12:51
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PassWordCheck {

    @Setter
    private static String username;
    @Setter
    private static String password;

    //basic方式登录
    public static boolean basicLogin(HttpRequest req) {
        if (username == null || password == null) {
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
            log.error("验证异常", e);
            return false;
        }
    }
}
