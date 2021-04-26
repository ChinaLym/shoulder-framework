package com.example.demo1.i18n;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Test;

/**
 * todo 改造使其可以自动化
 *
 * @see com.example.demo1.controller.i18n.ErrorCodeI18nDemoController#errorCode()
 */
public class I18nTest extends BaseWebTest {


    @Test
    public void test0() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"嗨\"}";
        doGetTest("/i18n/spring?toBeTranslate=shoulder.test.hi", result);
        doGetTest("/i18n/shoulder?toBeTranslate=shoulder.test.hi", result);
        doGetTest("/i18n/1?toBeTranslate=shoulder.test.hi", result);
    }

    @Test
    public void testErrorCode() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"0x00000064\":\"文件系统错误：创建文件失败\",\"0x000a0001\":\"user locked\",\"0x000a2712\":\"third service error\",\"0x0000000d\":\"认证无效，需要先进行认证\",\"0x000a2711\":\"age out of range\"}}";
        doGetTest("/i18n/errorCode", result);
    }
}
