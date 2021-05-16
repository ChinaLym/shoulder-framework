package com.example.demo1.guid;

import com.example.demo1.BaseWebTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GuidTest extends BaseWebTest {


    @Test
    public void test0() throws Exception {
        String instanceId = doGetTest("/guid/instanceId")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(Long.parseLong(instanceId) >= 0);
    }


    @Test
    public void testLong() throws Exception {
        String longGuid = doGetTest("/guid/long/1")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(Long.parseLong(longGuid) != 0);

        String longGuids = doGetTest("/guid/long/2?num=5")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String l : JsonUtils.parseObject(longGuids, new TypeReference<String[]>() {
        })) {
            Assertions.assertTrue(Long.parseLong(l) != 0);
        }

    }


    @Test
    public void testString() throws Exception {
        String stringGuid = doGetTest("/guid/string/1")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(StringUtils.isNotBlank(stringGuid));


        String stringGuids = doGetTest("/guid/string/2?num=5")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String s : JsonUtils.parseObject(stringGuids, new TypeReference<String[]>() {
        })) {
            Assertions.assertTrue(StringUtils.isNotBlank(s));
        }


    }

}
