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

    @Test
    public void testErrorCode() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"0x000a00000001\":\"用户已经被锁定\",\"0x000186a1\":\"报名者年龄不符合要求\\r\\n转为异常抛出时，记录 info 级别日志，若接口中抛出未捕获，返回客户端 400 状态码\",\"0x00000064\":\"文件系统错误：创建文件失败\",\"0x000186a2\":\"错误描述：第三方服务失败\",\"0x0000000d\":\"认证过期，需要重新认证\"}}";
        doGetTest("/i18n/errorCode", result);
    }
}
