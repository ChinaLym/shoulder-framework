package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;

import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

/**
 * 字符串 转 JDK8 提供的日期或时间类型，模板方法
 *
 * @author lym
 */
public abstract class BaseLocalDateTimeConverter<T> extends BaseDateConverter<T> {

    @Override
    protected T parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) {
        return parseFunction().apply(sourceDateString, DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

    /**
     * 解析时间的方式
     *
     * @return LocalDate/LocalDateTime parse
     */
    protected abstract BiFunction<String, DateTimeFormatter, T> parseFunction();


}

