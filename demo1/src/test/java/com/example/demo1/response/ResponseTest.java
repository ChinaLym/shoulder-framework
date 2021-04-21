package com.example.demo1.response;

import com.example.demo1.BaseControllerTest;
import org.junit.jupiter.api.Test;

/**
 * 响应格式测试
 *
 * @see com.example.demo1.controller.i18n.ErrorCodeI18nDemoController#errorCode()
 */
public class ResponseTest extends BaseControllerTest {

    @Test
    public void test0() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"data\"}";
        doGetTest("/response/0", result);
        doGetTest("/response/1", result);

    }

    @Test
    public void test2() throws Exception {
        doGetTest("/response/2", "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"1\":{\"id\":\"id1\",\"name\":\"name1\"},\"2\":{\"id\":\"id2\",\"name\":\"name2\"}}}");
    }

    @Test
    public void test3() throws Exception {
        doGetTest("/response/4", "{\"code\":\"0\",\"msg\":\"msg\",\"data\":\"data\",\"args\":[\"red\",\"black\"]}");
    }

}
