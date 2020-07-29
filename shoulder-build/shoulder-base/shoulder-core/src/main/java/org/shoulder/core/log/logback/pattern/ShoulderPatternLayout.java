package org.shoulder.core.log.logback.pattern;

import ch.qos.logback.classic.PatternLayout;

/**
 * 替换了 {@link PatternLayout} 的时间转换器类
 *
 * @author lym
 * @see PatternLayout
 */
public class ShoulderPatternLayout extends PatternLayout {

    public ShoulderPatternLayout() {
        super();
        super.getDefaultConverterMap().put("d", TimeZoneLazyAwareDateConverter.class.getName());
        super.getDefaultConverterMap().put("date", TimeZoneLazyAwareDateConverter.class.getName());
    }

}
