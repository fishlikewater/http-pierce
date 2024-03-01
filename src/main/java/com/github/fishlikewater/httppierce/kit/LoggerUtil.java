package com.github.fishlikewater.httppierce.kit;

import com.github.fishlikewater.httppierce.config.Constant;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.java.SimpleFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * 原生日志
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年04月12日 9:17
 **/
@Slf4j
public class LoggerUtil {

    private LoggerUtil() {}

    private static final Logger logger = Logger.getLogger(LoggerUtil.class.getName());

    @Setter
    private static String logPath = "";

    public static Logger getLogger() {
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String logPath = LoggerUtil.logPath + "-" + day;
        if (!Files.exists(Paths.get(logPath)) || logger.getHandlers().length == Constant.INT_ZERO) {
            init(logPath);
        }
        return logger;
    }

    private static void init(String logPath) {
        try {
            for (Handler handler : logger.getHandlers()) {
                handler.close();
                logger.removeHandler(handler);
            }

            logger.setLevel(Level.INFO);
            FileHandler handler = new FileHandler(logPath, true);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
            handler.setLevel(Level.INFO);
            logger.addHandler(handler);
        } catch (IOException e) {
            log.error("init fail", e);
        }
    }

    public static void error(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        getLogger().warning(sw.toString());
    }

    public static void info(String info) {
        getLogger().info(info);
    }
}
