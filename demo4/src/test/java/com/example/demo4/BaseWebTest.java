package com.example.demo4;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证是在过滤器中执行的，这里是测的 mvc，因此无法使用 MockMvc 测试
 * SpringBoot使用MockMvc：https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-mock-environment
 * <p>
 * 使用MockMvc对象进行请求，不会进行一个完整的请求(就是说不会经过过滤器，异常处理等操作)
 * 需要完整的请求请按  https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-running-server  这个文档使用即可
 * 为了测试 filter，采用随机端口
 */
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
public class BaseWebTest implements EnvironmentAware {

    protected Environment environment;

    @Autowired
    protected MockMvc mockMvc;

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate rest;

    private static final String UTF_8 = "UTF-8";


    public ResultActions doGetTest(String getUrl, String resultContains) throws Exception {
        return doGetTest(getUrl)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(resultContains)));
    }

    public ResultActions doGetTest(String getUrl) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(getUrl).characterEncoding(UTF_8));
        resultActions.andReturn().getResponse().setCharacterEncoding(UTF_8);
        return resultActions.andDo(print());
    }

    public ResultActions doPostTest(String postUrl, String resultContains) throws Exception {
        return doPostTest(postUrl, Collections.emptyMap(), resultContains);
    }


    public ResultActions uploadFile(String postUrl, MockMultipartFile file) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = multipart(postUrl)
                .file(file)
                .characterEncoding(UTF_8);

        ResultActions resultActions = mockMvc.perform(requestBuilder);
        resultActions.andReturn().getResponse().setCharacterEncoding(UTF_8);
        return resultActions.andDo(print());
    }

    public ResultActions doPostTest(String postUrl, Map<String, Object> params, String resultContains) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(postUrl)
                .characterEncoding(UTF_8)
                .contentType(MediaType.APPLICATION_JSON);
        if (MapUtils.isNotEmpty(params)) {
            requestBuilder.content(JsonUtils.toJson(params));
        }

        ResultActions resultActions = mockMvc.perform(requestBuilder);
        resultActions.andReturn().getResponse().setCharacterEncoding(UTF_8);
        return resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(resultContains)));
    }

    @Override
    public void setEnvironment(@Nonnull Environment environment) {
        this.environment = environment;
    }

}
