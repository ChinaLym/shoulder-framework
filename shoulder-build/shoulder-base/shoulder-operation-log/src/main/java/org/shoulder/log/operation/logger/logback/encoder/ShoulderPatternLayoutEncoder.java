package org.shoulder.log.operation.logger.logback.encoder;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;
import org.shoulder.log.operation.logger.logback.pattern.ShoulderPatternLayout;

/**
 * 与 logback 默认的 {@link PatternLayoutEncoder} 相比，提高了性能和增加了感知时区变化的能力
 *
 * @author lym
 * @see PatternLayoutEncoder
 */
public class ShoulderPatternLayoutEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {

    @Override
    public void start() {
        ShoulderPatternLayout shoulderPatternLayout = new ShoulderPatternLayout();
        shoulderPatternLayout.setContext(context);
        shoulderPatternLayout.setPattern(getPattern());
        shoulderPatternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
        shoulderPatternLayout.start();
        // 主要改动为 layout 不同
        this.layout = shoulderPatternLayout;
        super.start();
    }

}

