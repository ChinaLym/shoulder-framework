package com.example.demo3.token;

import org.junit.jupiter.api.*;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated 认证是在过滤器中执行的，这里是测的 mvc
 */
//@SpringBootTest(classes = TestBeanConfiguration.class)
public class AuthTest {

    private static final Logger log = LoggerFactory.getLogger(AuthTest.class);

    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Test
    void contextLoads() {
        // 测试能否启动
    }

    @BeforeAll
    public static void beforeAll() {
        log.info("AuthTest START.....");
    }

    @BeforeEach
    public void beforeEach() {
        log.info("beforeEach");  //mockMvc = MockMvcBuilders.standaloneSetup(new IndexController()).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    public void afterEach() {
        log.info("afterEach");
    }

    @AfterAll
    public static void afterAll() {
        log.info("AuthTest END.....");
    }

    /**
     * 测试认证接口
     * @see TokenAuthenticationSuccessHandler
     */
    @Test
    public void testAuth() throws Exception {
        Map<String, String> body = new HashMap<>(2);
        // 用户名
        body.put("username", "shoulder");
        // 密码
        body.put("password", "shoulder");
        String authorization = Base64.getEncoder().encodeToString("Basic shoulder:shoulder".getBytes());
        String response = restTemplate.postForObject("http://localhost:8080/authentication/form", body, String.class);
        RequestBuilder request = MockMvcRequestBuilders.post("/authentication/form")
                .header("Authorization", authorization)
                //请求类型 JSON
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(body))
                .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())     //期望的结果状态 200
                .andDo(MockMvcResultHandlers.print())                 //添加ResultHandler结果处理器，比如调试时 打印结果(print方法)到控制台
                .andReturn();                                         //返回验证成功后的MvcResult；用于自定义验证/下一步的异步处理；
        int status = mvcResult.getResponse().getStatus();                 //得到返回代码
        String content = mvcResult.getResponse().getContentAsString();    //得到返回结果
        log.info("status:" + status + ",content:" + content);
    }

}
