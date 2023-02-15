package com.github.fishlikewater.httppierce.server;


import sun.misc.Signal;

/**
 * <p>
 *     关闭后处理 释放资源
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月13日 22:08
 **/
public class ShutDownSignalHandler{

    public void registerSignal(String signalName, Boot... boot) {
        Signal signal = new Signal(signalName);
        Signal.handle(signal, (Signal sig)-> {
            for (Boot boot1 : boot) {
                boot1.stop();
            }
        });
    }
}
