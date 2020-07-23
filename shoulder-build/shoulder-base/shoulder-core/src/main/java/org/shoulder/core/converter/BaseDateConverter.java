package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Controller 入参为 Date 类型如何转换
 *
 * @param <T> 时间类
 * @author lym
 */
public abstract class BaseDateConverter<T> implements Converter<String, T> {

    /**
     * 具体的格式化表达式
     */
    private Map<String, String> formatMap = initTimeParserMap();

    /**
     * 用于初始化格式表达式 Map
     * @return key:时间转换格式，value:匹配正则
     */
    protected abstract Map<String, String> initTimeParserMap();

    /**
     * 转换
     * @param source 待转换字符串
     * @return 转换后的对象
     */
    @Override
    public T convert(@NonNull String source) {
        if (source.isEmpty()) {
            return null;
        }
        source = source.trim();
        // 匹配模板
        for (Map.Entry<String, String> entry : formatMap.entrySet()) {
            if (source.matches(entry.getValue())) {
                return parseDateOrTime(source, entry.getKey());
            }
        }
        throw new IllegalArgumentException("invalid time format:'" + source + "'");
    }

    /**
     * 根据特定模板，将 sourceDataString 转化为时间类
     * @param sourceDataString 时间字符串，如 '2020-01-01'
     * @param dateTimeTemplate 时间格式，如 'yyyy-mm-dd'
     * @return 日期时间类对象
     * @throws  DateTimeParseException 日期转换出错
     */
    protected abstract T parseDateOrTime(@NotEmpty String sourceDataString, String dateTimeTemplate) throws DateTimeParseException;

}
