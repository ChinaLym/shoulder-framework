package org.shoulder.auth.uaa.endpoint;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.interfaces.RSAPublicKey;
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

    /**
     * 只支持 RSA
     */
    private final AsymmetricCryptoProcessor rsa2048;

    private Map<String, Object> cache;

    public JwkSetEndpoint(KeyPairCache keyPairCache) {
        this.rsa2048 = DefaultAsymmetricCryptoProcessor.rsa2048(keyPairCache);

    }

    @GetMapping(PUBLIC_KEY_END_POINT)
    @ResponseBody
    public Map<String, Object> getKey() {
        return genKeySet();
    }

    private Map<String, Object> genKeySet() {
        if (cache == null) {
            try {
                RSAPublicKey publicKey = (RSAPublicKey) this.rsa2048.getPublicKey("jwk");
                RSAKey key = new RSAKey.Builder(publicKey).build();
                cache = new JWKSet(key).toJSONObject();
            } catch (KeyPairException e) {
                throw new BaseRuntimeException("genKeySet fail.");
            }
        }
        return cache;

    }


}
