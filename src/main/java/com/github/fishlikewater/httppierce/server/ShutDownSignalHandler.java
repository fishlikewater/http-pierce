package com.github.fishlikewater.httppierce.server;


import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * <p>
 *     关闭后处理 释放资源
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月13日 22:08
 **/
public class ShutDownSignalHandler implements SignalHandler {

    public void registerSignal(String signalName) {
        Signal signal = new Signal(signalName);
        Signal.handle(signal, this);
    }

    @Override
    public void handle(Signal sig) {

    }
}
