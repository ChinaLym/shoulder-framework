package org.shoulder.security.authentication.endpoint;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 标准授权服务器（spring-security-oauth2）不支持 jwk 相关 url
 * <a href target="_blank" href="https://tools.ietf.org/html/rfc7517#section-5">JWK Set</a> endpoint.
 * <p>
 * 为更简单/方便地支持使用，特地支持
 * JWK 接口用处（便于动态分发服务端公钥，常用于 AuthServer 中）：
 *      客户端或资源服务器可以通过这个接口获取到公钥，并用它来验证JWT的签名，确保它是由合法的服务发出的，没有被篡改过。
 *      密钥交换/协商建立安全信道
 *      密钥不需要手动分发，而是自动定期轮换
 *      不同租户/用户公钥隔离
 *      客户端验证签名
 * 与 {@link IntrospectEndpoint}相比，更适合客户端能力更强更可信（可以校验token，缓存公钥，高性能）的场景
 *
 * @author lym
 */
@RestController
public class JwkSetEndpoint {

    /**
     * 获取公钥信息，开放端点
     */
    public static final String PUBLIC_KEY_END_POINT = "/.well-known/jwks.json";

    /**
     * keyPairId，默认只有一个，可以存在多个
     */
    private final JWKSet jwkSet;

    public JwkSetEndpoint(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    /**
     * 查看接口返回结果 http://localhost:8080/.well-known/jwks.json
     */
    @GetMapping(PUBLIC_KEY_END_POINT)
    public Map<String, Object> getKey() {
        return jwkSet.toJSONObject();
    }

}
