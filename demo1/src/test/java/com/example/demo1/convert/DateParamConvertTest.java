package com.example.demo1.convert;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Test;

class DateParamConvertTest extends BaseWebTest {

    @Test
    public void test1() throws Exception {
        doGetTest("/date/1?date=2020",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"2020-01-01T00:00:00.000 +0800\"");
    }

    @Test
    public void test2() throws Exception {
        doGetTest("/date/2?date=2020-1-01",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"2020-01-01\"");
    }

    @Test
    public void test3() throws Exception {
        doGetTest("/date/3?date=2020-1-01 12:20:13",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"2020-01-01T12:20:13\"");
    }

    @Test
    public void test4() throws Exception {
        doGetTest("/date/4?date=12:20:13",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"12:20:13\"");
    }

    @Test
    public void test0() throws Exception {
        doGetTest("/enum/0?color=RED",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"RED\"");
    }

}
