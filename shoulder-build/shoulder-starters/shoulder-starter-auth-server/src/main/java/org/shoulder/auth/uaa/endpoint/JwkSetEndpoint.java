package org.shoulder.auth.uaa.endpoint;

import com.nimbusds.jose.jwk.JWKSet;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 标准授权服务器（spring-security-oauth2）不支持 jwk 相关 url
 * <a href target="_blank" href="https://tools.ietf.org/html/rfc7517#section-5">JWK Set</a> endpoint.
 * <p>
 * 为更简单/方便地支持使用，特地支持
 *
 * @author lym
 */
@SkipResponseWrap
@RestController
public class JwkSetEndpoint {

    /**
     * 获取公钥信息，开放端点
     */
    public static final String PUBLIC_KEY_END_POINT = "/.well-known/jwks.json";

    /**
     * keyPairId，默认只有一个，可以存在多个
     */
    private JWKSet jwkSet;

    public JwkSetEndpoint(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    /**
     * 查看接口返回结果 http://localhost:8080/.well-known/jwks.json
     */
    @GetMapping(PUBLIC_KEY_END_POINT)
    @ResponseBody
    public Map<String, Object> getKey() {
        return jwkSet.toJSONObject();
    }

}
