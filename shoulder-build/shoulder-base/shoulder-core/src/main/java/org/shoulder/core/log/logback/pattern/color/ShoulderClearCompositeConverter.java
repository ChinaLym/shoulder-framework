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
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return BOLD + RED_FG;
            case Level.WARN_INT:
                return BLUE_FG;
/*            case Level.INFO_INT:
                return DEFAULT_FG;*/
            case Level.DEBUG_INT:
                return WHITE_FG;
            default:
                return DEFAULT_FG;
        }

    }
}
