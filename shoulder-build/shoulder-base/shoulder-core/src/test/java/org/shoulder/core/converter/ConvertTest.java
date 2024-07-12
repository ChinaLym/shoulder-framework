package org.shoulder.core.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.dictionary.ColorIntEnum;
import org.shoulder.core.util.ConvertUtil;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.ConversionFailedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lym
 */
public class ConvertTest {

    private static final WebConversionService springConversion = new WebConversionService(new DateTimeFormatters()
            .dateFormat("iso")
            .timeFormat("iso")
            .dateTimeFormat("iso"));

    private static LocalDateConverter converter = new LocalDateConverter();


    @Test
    public void testStrToDictionaryEnum() {
        ConvertUtil.addConverterFactory(EnumConverterFactory.getDefaultInstance());

        Assertions.assertNull(ConvertUtil.convert(null, ColorIntEnum.class));
        Assertions.assertNull(ConvertUtil.convert("", ColorIntEnum.class));
        Assertions.assertEquals(ColorIntEnum.GRAY, ConvertUtil.convert("GRAY", ColorIntEnum.class));
        Assertions.assertNull(ConvertUtil.convert("128", ColorIntEnum.class));
    }


    /**
     * 支持多种格式字符串转日期
     */
    @Test
    public void testDateConvert() {
        // LocalTime
        Assertions.assertEquals(LocalTime.parse("11:12:13.123"), ConvertUtil.convert("11:12:13.123", LocalTime.class));
        Assertions.assertEquals(LocalTime.parse("11:12:13"), ConvertUtil.convert("11:12:13", LocalTime.class));

        // LocalDate
        LocalDate localDate = LocalDate.of(2020, 5, 1);

        Instant instantLocal = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // 支持多种格式
        Assertions.assertEquals(localDate, ConvertUtil.convert(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() + "", LocalDate.class));
        Assertions.assertEquals(LocalTime.parse("00:00:00"), ConvertUtil.convert(instantLocal.toEpochMilli() + "", LocalTime.class));


        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-5-1", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-5-01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020-05-01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/5/1", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/5/01", LocalDate.class));
        Assertions.assertEquals(localDate, ConvertUtil.convert("2020/05/01", LocalDate.class));


        Assertions.assertNull(ConvertUtil.convert(null, LocalDate.class));
        Assertions.assertThrows(ConversionFailedException.class, () -> ConvertUtil.convert("null", LocalDate.class));
        Assertions.assertThrows(ConversionFailedException.class, () -> ConvertUtil.convert("2020-05-01T", LocalDate.class));


        // Date
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        compareConvertDate(date, Date.class);
        Assertions.assertEquals(date, ConvertUtil.convert(instantLocal.toEpochMilli() + "", Date.class));
        Assertions.assertThrows(ConversionFailedException.class, () -> ConvertUtil.convert("6666-666-66", Date.class));
        compareConvertDateTime(date, Date.class);

        // LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        Assertions.assertEquals(localDateTime, ConvertUtil.convert(instantLocal.toEpochMilli() + "", LocalDateTime.class));
        compareConvertDateTime(localDateTime, LocalDateTime.class);
        Assertions.assertEquals(localDateTime, ConvertUtil.convert("2020-05-01T00:00:00+8", LocalDateTime.class));
        Assertions.assertEquals(localDateTime, ConvertUtil.convert("2020-05-01T00:00:00-8", LocalDateTime.class));
    }


    /**
     * 支持多种格式字符串转日期
     */
    @Test
    public void testListConvert() {
        List<String> source = null;
        Assertions.assertNull(ConvertUtil.convert(source, String.class));
        source = new LinkedList<>();
        source.add(null);
        source.add("1");
        List<Integer> integerList = ConvertUtil.convert(source, Integer.class);
        Assertions.assertNull(integerList.get(0));
        Assertions.assertEquals(1, integerList.get(1));
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
