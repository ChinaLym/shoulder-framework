package com.example.demo6.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * 适用于 JWT
 * spring-security-oauth2 授权服务器 未支持任何 jwk 相关端点 url
 * <a href target="_blank" href="https://tools.ietf.org/html/rfc7517#section-5">JWK Set</a> endpoint.
 * <p>
 * 本类特地支持该 endPoint，以更好地支持的其他样本的回调。
 * 查看该类返回结果 http://localhost:8080/.well-known/jwks.json
 *
 * @author lym
 */
@SkipResponseWrap
@FrameworkEndpoint
public class JwkSetEndpoint {

    @Autowired
    private KeyPair keyPair;

    private static Map<String, Object> cache;

    @GetMapping("/.well-known/jwks.json")
    @ResponseBody
    public Map<String, Object> getKey() {
        return genKeySet();
    }


    private Map<String, Object> genKeySet() {
        if (cache == null) {
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAKey key = new RSAKey.Builder(publicKey).build();
            cache = new JWKSet(key).toJSONObject();
        }
        return cache;

    }
}
