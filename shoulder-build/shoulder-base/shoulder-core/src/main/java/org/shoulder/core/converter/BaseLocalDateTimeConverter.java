package org.shoulder.core.converter;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

/**
 * 字符串转 JDK8 提供的日期或时间类型，模板方法
 *
 * @author lym
 */
public abstract class BaseLocalDateTimeConverter<T> extends BaseDateConverter<T> {

    @Override
    protected T parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) {
        return parseFunction().apply(toStandFormat(sourceDateString), DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

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

    /**
     * 解析时间的方式
     *
     * @return LocalDate/LocalDateTime parse
     */
    protected abstract BiFunction<String, DateTimeFormatter, T> parseFunction();

    /**
     * 转为标准的 yyyy-MM-dd
     *
     * @param yyyyMMdd 非严格 yyyy-MM-dd 或 yyyy/MM/dd 格式的日期字符串
     * @return 严格格式
     */
    @SuppressWarnings("PMD.LowerCamelCaseVariableNamingRule")
    protected String toStandYearMonthDay(@Nonnull String yyyyMMdd) {
        final int stdFormatLength = 10;
        if (yyyyMMdd.length() == stdFormatLength) {
            return yyyyMMdd;
        } else {
            String split = yyyyMMdd.contains("-") ? "-" : "/";
            String[] timePart = yyyyMMdd.split(split);
            assert timePart.length == 3;
            String year = timePart[0];
            String month = timePart[1];
            String day = timePart[2];
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

