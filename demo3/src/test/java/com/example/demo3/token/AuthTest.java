package com.example.demo3.token;

import com.example.demo3.entity.UserEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.security.authentication.handler.json.TokenAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * 认证是在过滤器中执行的，这里是测的 mvc，因此无法使用 MockMvc 测试
 * SpringBoot使用MockMvc：https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-mock-environment
 *
 * 使用MockMvc对象进行请求，不会进行一个完整的请求(就是说不会经过过滤器，异常处理等操作)
 *
 * 需要完整的请求请按  https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-running-server  这个文档使用即可
 */
//@SpringBootTest // 测试前注意：为了提高测试类启动速度，默认未启动上下文，推荐另外运行一个服务，也方便debug
public class AuthTest {

    private static final Logger log = LoggerFactory.getLogger(AuthTest.class);

    private RestTemplate restTemplate = new RestTemplate();


    @Test
    void contextLoads() {
        // 测试能否启动
    }

    @BeforeAll
    public static void beforeAll() {
        log.info("AuthTest START.....");
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
     *
     * @see TokenAuthenticationSuccessHandler 认证成功发 token
     */
    @Test
    public void testAuth() throws Exception {
        Map<String, Object> accessToken = getAccessToken();
        assert accessToken.get("access_token") != null;
        assert "bearer".equals(accessToken.get("token_type"));
        assert ((int) accessToken.get("expires_in")) > 0;
        assert "scopes".equals(accessToken.get("scope"));

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAccessToken() {
        String url = "http://localhost:8080/authentication/form";
        String authorization = "Basic " + Base64.getEncoder().encodeToString("shoulder:shoulder".getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", authorization);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "shoulder");
        formData.add("password", "shoulder");
        HttpEntity requestBody = new HttpEntity<>(formData, headers);

        ResponseEntity<RestResult> responseEntity = restTemplate.postForEntity(url, requestBody, RestResult.class);
        assert responseEntity.getStatusCode() == HttpStatus.OK;
        RestResult authResult = responseEntity.getBody();
        log.info("status:" + responseEntity.getStatusCodeValue() + ",content:" + JsonUtils.toJson(authResult));
        assert authResult != null;
        return (Map<String, Object>) authResult.getData();
    }

    /**
     * 测试认证后调用访问资源接口
     *
     * @see ProviderManager 根据传入的token认证
     * @see org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider 实际认证处理器
     * @see org.springframework.security.web.access.intercept.FilterSecurityInterceptor
     */
    @Test
    public void testGetResource() throws Exception {
        String url = "http://localhost:8080//user/1";
        String token = (String) getAccessToken().get("access_token");
        String authorization = "Bearer " + token;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorization);
        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<UserEntity> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, UserEntity.class);

        log.info(JsonUtils.toJson(response));
    }

}
