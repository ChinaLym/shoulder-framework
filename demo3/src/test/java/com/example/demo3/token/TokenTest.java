package com.example.demo3.token;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo3.BaseWebTest;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.service.IUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.security.authentication.handler.json.FormTokenAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证是在过滤器中执行的，这里是测的 mvc，因此无法使用 MockMvc 测试
 * SpringBoot使用MockMvc：https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-mock-environment
 * <p>
 * 使用MockMvc对象进行请求，不会进行一个完整的请求(就是说不会经过过滤器，异常处理等操作)
 * <p>
 * 需要完整的请求请按  https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing-spring-boot-applications-testing-with-running-server  这个文档使用即可
 */
public class TokenTest extends BaseWebTest {

    protected boolean isTokenMode() {
        return ArrayUtils.contains(environment.getActiveProfiles(), "token");
    }

    @Autowired
    private IUserService userService;

    @Test
    public void requireAccessTokenOrForbiddenTest() throws Exception {
        if (!isTokenMode()) {
            return;
        }
        // todo
        doGetTest("/")
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(equalTo("{\"code\":\"0x0000000d\",\"msg\":\"" + CommonErrorCodeEnum.AUTH_401_EXPIRED.getMessage() + "\",\"data\":null}")));
    }

    /**
     * 测试认证接口
     *
     * @see FormTokenAuthenticationSuccessHandler 认证成功发 token
     */
    @Test
    public void getAccessTokenTest() {
        if (!isTokenMode()) {
            return;
        }
        String accessToken = getAccessToken();
        String repeatTest = getAccessToken();
        System.out.println(accessToken);

        Assertions.assertEquals(accessToken, repeatTest);
    }


    /**
     * 测试认证后调用访问资源接口
     *
     * @see ProviderManager 根据传入的token认证
     * @see org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider 实际认证处理器
     * @see org.springframework.security.web.access.intercept.FilterSecurityInterceptor
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void testGetResource() {
        String token = getAccessToken();
        String authorization = "Bearer " + token;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorization);
        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<UserEntity> response = rest.exchange("/user/1", HttpMethod.GET, requestEntity, UserEntity.class);
        UserEntity userEntity = userService.getById(1);

        Assertions.assertEquals(userEntity.toString(), Objects.requireNonNull(response.getBody()).toString());

        String name = "Shoulder";
        ResponseEntity<UserEntity> response2 = rest.exchange("/user/getOne?name=" + name, HttpMethod.GET, requestEntity,
                UserEntity.class);
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getName, name);
        UserEntity userEntity2 = userService.getOne(queryWrapper);
        Assertions.assertEquals(userEntity2.toString(), Objects.requireNonNull(response2.getBody()).toString());
    }

    @SuppressWarnings("rawtypes, unchecked")
    private String getAccessToken() {
        String url = "/authentication/form";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "shoulder");
        formData.add("password", "shoulder");

        ResponseEntity<BaseResult> responseEntity = rest.postForEntity(url, formData, BaseResult.class);

        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

        BaseResult authResult = responseEntity.getBody();

        System.out.println("status:" + responseEntity.getStatusCodeValue() + ",content:" + JsonUtils.toJson(authResult));
        Assertions.assertNotNull(authResult);

        Map<String, Object> result = (Map<String, Object>) authResult.getData();
        String accessToken = (String) result.get("access_token");

        Assertions.assertNotNull(authResult);
        Assertions.assertEquals(result.get("token_type"), "bearer");
        Assertions.assertEquals(result.get("scope"), "scopes");
        Assertions.assertTrue(((int) result.get("expires_in")) > 0);

        return accessToken;
    }


}
