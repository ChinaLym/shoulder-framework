package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;
import org.shoulder.core.util.RegexpUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
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
        // 匹配模板
        source = source.replace("T", " ");

        for (Map.Entry<String, String> entry : formatMap.entrySet()) {
            String template = entry.getKey();
            if (RegexpUtils.matches(source, entry.getValue())) {
                String formatSource = source.length() != template.length() ? toStandFormat(source.trim()) : source;
                return parseDateOrTime(formatSource, template);
            }
        }

        if (RegexpUtils.matches(source, "1\\d{9,12}")) {
            // 时间戳格式
            long l = Long.parseLong(source);
            Instant instant = source.length() == 10 ? Instant.ofEpochSecond(l) : Instant.ofEpochMilli(l);
            return fromInstant(instant);
        }
        throw new IllegalArgumentException("invalid time format:'" + source + "'");
    }

    protected abstract T fromInstant(Instant instant);

    /**
     * 根据特定模板，将 sourceDateString 转化为时间类
     *
     * @param sourceDateString 时间字符串，如 '2020-01-01'
     * @param dateTimeTemplate 时间格式，如 'yyyy-mm-dd'
     * @return 日期时间类对象
     * @throws DateTimeParseException 日期转换出错
     */
    protected abstract T parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) throws DateTimeParseException;

    /**
     * 将不标准的时间格式转为标准的，如 2020-1-1 转为 2020-01-01
     *
     * @param sourceDateString 输入的时间字符串
     * @return 可解析的时间格式
     */
    @Nonnull
    protected String toStandFormat(@Nonnull String sourceDateString) {
        return sourceDateString;
    }

    @Nonnull
    protected static String toStandDateFormat(@Nonnull String sourceDateString, boolean ignoreTimeZone) {
        if (sourceDateString.length() == 19 || sourceDateString.length() == 23 || sourceDateString.length() == 9 || sourceDateString.length() == 12) {
            // 大多数情况正常，或是时间戳格式，不需要额外格式化
            return sourceDateString;
        }
        // format 长度
        int splitIndex = -1;
        int tzIndex = -1;
        if (!sourceDateString.contains(" ") && !sourceDateString.contains("T") && sourceDateString.length() >= 8 && sourceDateString.length() <= 10) {
            // 只有年月日，则尝试添加0点时间转换
            sourceDateString += " 00:00:00";
        } else if (ignoreTimeZone && (tzIndex = sourceDateString.indexOf('+')) > 0) {
            // 忽略时区
            sourceDateString = sourceDateString.substring(0, tzIndex);
        } else if (ignoreTimeZone && (tzIndex = StringUtils.lastIndexOf(sourceDateString, '-')) > sourceDateString.indexOf(':')) {
            // 忽略时区
            sourceDateString = sourceDateString.substring(0, tzIndex);
        }
        splitIndex = splitIndex > 0 ? splitIndex : sourceDateString.indexOf(' ');

        if (splitIndex < 0) {
            return toStandYearMonthDay(sourceDateString);
        }
        if (splitIndex == sourceDateString.length() - 1) {
            throw new IllegalArgumentException("not support such date format: " + sourceDateString);
        }
        String date = sourceDateString.substring(0, splitIndex);
        String time = sourceDateString.substring(splitIndex + 1);
        return toStandYearMonthDay(date) + " " + time;
    }

    /**
     * 转为标准的 yyyy-MM-dd
     *
     * @param yyyyMMdd 非严格 yyyy-MM-dd 或 yyyy/MM/dd 格式的日期字符串
     * @return 严格格式
     */
    @SuppressWarnings("PMD.LowerCamelCaseVariableNamingRule")
    protected static String toStandYearMonthDay(@Nonnull String yyyyMMdd) {
        final int stdFormatLength = 10;
        if (yyyyMMdd.length() == stdFormatLength) {
            return yyyyMMdd;
        } else {
            if (yyyyMMdd.contains("-")) {
                return toStdDateFormat(yyyyMMdd.split("-"), "-");
            }
            if (yyyyMMdd.contains("/")) {
                return toStdDateFormat(yyyyMMdd.split("/"), "/");
            }
            return yyyyMMdd;
        }
    }

    private static String toStdDateFormat(String[] timePart, String split) {
        String year = timePart[0];
        String month = timePart[1];
        String day = timePart.length == 3 ? timePart[2] : "";
        StringBuilder stdDateFormat = new StringBuilder(year);
        stdDateFormat.append(split);
        if (month.length() == 1) {
            stdDateFormat.append("0");
        }
        stdDateFormat.append(month);
        stdDateFormat.append(split);
        if (day.length() == 1) {
            stdDateFormat.append("0");
        }
        stdDateFormat.append(day);
        return stdDateFormat.toString();
    }

    /**
     * 转为标准的 HH:mm:ss
     * @param HHmmss 非严格格式的时间字符串
     * @return 严格格式
     * @note JDK 支持非严格格式，无需此方法
     */
    /*protected String toStandHourMinuteSecond(@Nonnull String HHmmss) {
        final int stdFormatLength = 8;
        if(HHmmss.length() == stdFormatLength) {
            return HHmmss;
        }else {
            String[] timePart = HHmmss.split(":");
            assert timePart.length == 3;
            String hour = timePart[0];
            String minute = timePart[1];
            String second = timePart[2];
            StringBuilder stdDateFormat = new StringBuilder();
            if(minute.length() == 1){
                stdDateFormat.append("0");
            }
            stdDateFormat.append(hour);
            stdDateFormat.append(":");
            if(minute.length() == 1){
                stdDateFormat.append("0");
            }
            stdDateFormat.append(minute);
            stdDateFormat.append(":");
            if(second.length() == 1){
                stdDateFormat.append("0");
            }
            stdDateFormat.append(second);
            return stdDateFormat.toString();
        }
    }*/

}
