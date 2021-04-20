package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 字符串转日期或时间类型，模板方法【注意，spring boot 2.3 之后内置支持了】
 *
 * @param <T> 时间类
 * @author lym
 */
public abstract class BaseDateConverter<T> implements Converter<String, T> {

    /**
     * 具体的格式化表达式
     */
    private final Map<String, String> formatMap = initTimeParserMap();

    /**
     * 用于初始化格式表达式 Map
     *
     * @return key:时间转换格式，value:匹配正则
     */
    protected abstract Map<String, String> initTimeParserMap();

    /**
     * 转换
     *
     * @param source 待转换字符串
     * @return 转换后的对象
     */
    @Override
    public T convert(@Nonnull String source) {
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
     * 根据特定模板，将 sourceDateString 转化为时间类
     *
     * @param sourceDateString 时间字符串，如 '2020-01-01'
     * @param dateTimeTemplate 时间格式，如 'yyyy-mm-dd'
     * @return 日期时间类对象
     * @throws DateTimeParseException 日期转换出错
     */
    protected abstract T parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) throws DateTimeParseException;

}
