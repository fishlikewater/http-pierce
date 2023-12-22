package com.github.fishlikewater.httppierce.kit;

import io.netty.channel.epoll.Epoll;

/**
 * @author zhangx
 * @version V1.0
 **/
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
