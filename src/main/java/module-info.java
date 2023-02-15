/**
 * <p>
 *  模块化配置文件
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 23:03
 **/
open module http.pierce {

    requires spring.boot;
    requires spring.boot.starter;
    requires spring.boot.autoconfigure;
    requires spring.boot.configuration.processor;
    requires io.netty.all;
    requires lombok;
    requires hutool.all;
    requires spring.context;
    requires jdk.unsupported;
    requires org.slf4j;
}