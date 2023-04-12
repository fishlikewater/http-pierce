package com.github.fishlikewater.httppierce.kit;

import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * <p>
 * 原生日志
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年04月12日 9:17
 **/
public class LoggerUtil {

    private static final Logger logger = Logger.getLogger(LoggerUtil.class.getName());

    @Setter
    private static String logPath = "";

    public static Logger getLogger() {
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String logPath = LoggerUtil.logPath + "-" + day;
        if (!Files.exists(Paths.get(logPath)) || logger.getHandlers().length == 0) {
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
            e.printStackTrace();
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
