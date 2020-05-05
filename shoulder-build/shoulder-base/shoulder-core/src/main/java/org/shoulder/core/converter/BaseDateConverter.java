package org.shoulder.core.converter;

import java.util.Map;
import java.util.function.Function;

/**
 * 解决入参为 Date类型
 *
 * @param <T> 时间类
 * @author lym
 */
public abstract class BaseDateConverter<T> {

    /**
     * 转换
     * @param source 待转换字符串
     * @param dateParser 转换规则
     * @return 转换后的对象
     */
    public T convert(String source, Function<String, T> dateParser) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        source = source.trim();
        for (Map.Entry<String, String> entry : getSupportPatterns().entrySet()) {
            if (source.matches(entry.getValue())) {
                return dateParser.apply(entry.getKey());
            }
        }
        throw new IllegalArgumentException("invalid time format:'" + source + "'");
    }

    /**
     * 获取子类 具体的格式化表达式
     */
    protected abstract Map<String, String> getSupportPatterns();
}
