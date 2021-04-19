package com.example.demo1.i18n;

import com.example.demo1.BaseControllerTest;
import org.junit.jupiter.api.Test;

/**
 * todo 改造使其可以自动化
 *
 * @see com.example.demo1.controller.i18n.ErrorCodeI18nDemoController#errorCode()
 */
public class I18nTest extends BaseControllerTest {


    @Test
    public void test0() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"嗨\"}";
        doGetTest("/i18n/spring?toBeTranslate=shoulder.test.hi", result);
        doGetTest("/i18n/shoulder?toBeTranslate=shoulder.test.hi", result);
        doGetTest("/i18n/1?toBeTranslate=shoulder.test.hi", result);
    }

}
