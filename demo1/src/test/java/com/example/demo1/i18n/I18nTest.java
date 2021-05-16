package com.example.demo1.i18n;

import com.example.demo1.BaseWebTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.util.JsonUtils;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        String jsonResult = doGetTest("/i18n/errorCode")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"0\"")))
                .andExpect(content().string(containsString("\"msg\":\"success\"")))
                .andReturn().getResponse().getContentAsString();
        BaseResult<Map<String, String>> r = JsonUtils.parseObject(jsonResult, new TypeReference<>() {
        });
        Map<String, String> trErrorCodeMap = r.getData();
        Assertions.assertEquals(trErrorCodeMap.get("0x0000000d"), "认证无效，需要先进行认证");
        Assertions.assertEquals(trErrorCodeMap.get("0x00000064"), "文件系统错误：创建文件失败");
        Assertions.assertEquals(trErrorCodeMap.get("0x000a0001"), "user locked");
        Assertions.assertEquals(trErrorCodeMap.get("0x000a2711"), "age out of range");
        Assertions.assertEquals(trErrorCodeMap.get("0x000a2712"), "third service error");
        ;
    }
}
