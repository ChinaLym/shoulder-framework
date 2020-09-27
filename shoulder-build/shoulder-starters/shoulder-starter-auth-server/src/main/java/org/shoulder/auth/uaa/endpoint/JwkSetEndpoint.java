package org.shoulder.auth.uaa.endpoint;

import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 标准授权服务器（spring-security-oauth2）不支持 jwk 相关 url
 * <a href target="_blank" href="https://tools.ietf.org/html/rfc7517#section-5">JWK Set</a> endpoint.
 * <p>
 * 为更简单/方便地支持使用，特地支持
 *
 * @author lym
 */
@FrameworkEndpoint
public class JwkSetEndpoint {

    /**
     * 获取公钥信息，开放端点
     */
    public static final String PUBLIC_KEY_END_POINT = "/.well-known/jwks.json";

    private AsymmetricCryptoProcessor processor;

    @GetMapping(PUBLIC_KEY_END_POINT)
    @ResponseBody
    public Map<String, Object> getKey() throws KeyPairException {
        /*ECPublicKey publicKey = (ECPublicKey) processor.getPublicKey("default");
        ECKey key = new ECKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();*/
        return null;
    }
}
