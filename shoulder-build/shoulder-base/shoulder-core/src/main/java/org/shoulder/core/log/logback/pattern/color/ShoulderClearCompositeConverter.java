package org.shoulder.core.log.logback.pattern.color;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

/**
 * Shoulder 定义的彩色日志转换器，专门为控制台中输出彩色日志做转换，比如 启动LOGO
 *
 * @author lym
 */
public class ShoulderClearCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        return switch (level.toInt()) {
            // ERROR 级别加粗、红色
            case Level.ERROR_INT -> BOLD + RED_FG;
            // WARN 蓝色
            case Level.WARN_INT -> BLUE_FG;
            // INFO 默认色
/*            case Level.INFO_INT:
                return DEFAULT_FG;*/
            // DEBUG 浅色
            case Level.DEBUG_INT -> WHITE_FG;
            default -> DEFAULT_FG;
        };

    }
}
