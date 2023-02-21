package com.github.fishlikewater.httppierce.server;


import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import sun.misc.Signal;

import java.util.Map;

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
            final Map<String, DynamicHttpBoot> dynamicHttpBoot = ChannelUtil.DYNAMIC_HTTP_BOOT;
            if (dynamicHttpBoot.size() > 0){
                dynamicHttpBoot.forEach((k, v)->v.stop());
            }
            for (Boot boot1 : boot) {
                boot1.stop();
            }
            System.exit(0);
        });
    }
}
