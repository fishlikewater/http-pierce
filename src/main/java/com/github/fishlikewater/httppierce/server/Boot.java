package com.github.fishlikewater.httppierce.server;

/**
 * <p>
 * 启动类接口
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月15日 20:01
 **/
public interface Boot {

    /**
     * 启动服务
     *
     * @author fishlikewater@126.com
     * @since 2023/2/16 16:24
     */
    void start();

    /**
     * 停止服务
     *
     * @author fishlikewater@126.com
     * @since 2023/2/16 16:24
     */
    void stop();

}
