package org.shoulder.core.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.util.ConvertUtil;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author lym
 */
public class ConvertTest {

    private static final WebConversionService springConversion = new WebConversionService(new DateTimeFormatters()
            .dateFormat("iso")
            .timeFormat("iso")
            .dateTimeFormat("iso"));

    @Test
    public void testDateConvert() {
        Assertions.assertEquals(LocalTime.parse("11:12:13.123"), ConvertUtil.convert("11:12:13.123", LocalTime.class));
        Assertions.assertEquals(LocalTime.parse("11:12:13"), ConvertUtil.convert("11:12:13", LocalTime.class));

        LocalDate localDate = LocalDate.of(2020, 5, 1);
        // 支持多种格式
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-5-1", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-5-01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-05-01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/5/1", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/5/01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/05/01", LocalDate.class));


        // 支持多种格式 转 Date
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        compareConvertDate(date, Date.class);
        compareConvertDateTime(date, Date.class);

        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        compareConvertDateTime(localDateTime, LocalDateTime.class);


    }

    private static void compareConvertDate(Object date, Class<?> dateClass) {
        Assertions.assertEquals(date, ConvertUtil.convert("2020-05-01", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020/5/1", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020/5/01", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020/05/01", dateClass));

        Assertions.assertEquals(date, ConvertUtil.convert("2020-5-1", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020-5-01", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020-05-01", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020-05", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020-5", dateClass));
        Assertions.assertEquals(Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()), ConvertUtil.convert("2020", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020/5", dateClass));
        Assertions.assertEquals(date, ConvertUtil.convert("2020/05", dateClass));
    }

    private static void compareConvertDateTime(Object dateTime, Class<?> dateClass) {

        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-1 00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-01 00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-05-01 00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/1 00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/01 00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/05/01 00:00:00", dateClass));

        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-1 00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-01 00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-05-01 00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/1 00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/01 00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/05/01 00:00:00.000", dateClass));

        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-1T00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-01T00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-05-01T00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/1T00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/01T00:00:00", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/05/01T00:00:00", dateClass));

        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-1T00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-5-01T00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020-05-01T00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/1T00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/5/01T00:00:00.000", dateClass));
        Assertions.assertEquals(dateTime, ConvertUtil.convert("2020/05/01T00:00:00.000", dateClass));
    }
}
