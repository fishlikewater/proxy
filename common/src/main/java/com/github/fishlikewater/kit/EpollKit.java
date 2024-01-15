package com.github.fishlikewater.kit;

import io.netty.channel.epoll.Epoll;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author fishlikewater@126.com
 * @version V1.0
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpollKit {

    /**
     * 判断当前系统是否支持epoll
     *
     * @return boolean
     */
    public static boolean epollIsAvailable() {
        boolean available = Epoll.isAvailable();
        boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
        return available && linux;
    }
}
