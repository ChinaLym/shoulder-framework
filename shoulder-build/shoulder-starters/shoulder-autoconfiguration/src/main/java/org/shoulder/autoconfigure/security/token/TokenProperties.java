package org.shoulder.autoconfigure.security.token;

import org.shoulder.security.SecurityConst;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.temporal.ChronoUnit;

/**
 * OAuth2 认证相关配置
 *
 * @author lym
 */
@ConfigurationProperties(prefix = SecurityConst.CONFIG_PREFIX + ".token")
public class TokenProperties {

    /**
     * 使用jwt时，为token签名的密钥对标识，重要！
     */
    private String jwtSigningKey = "shoulder";

    /**
     * jwt/jdbc/redis/memory
     */
    private String store = "jwt";

    /**
     * jwt/jwk
     */
    private boolean jwk = true;

    /**
     * 客户端配置
     */
    private OAuth2ClientProperties[] clients = {};

    public OAuth2ClientProperties[] getClients() {
        return clients;
    }

    public void setClients(OAuth2ClientProperties[] clients) {
        this.clients = clients;
    }

    public String getJwtSigningKey() {
        return jwtSigningKey;
    }

    public void setJwtSigningKey(String jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public boolean isJwk() {
        return jwk;
    }

    public void setJwk(Boolean jwk) {
        this.jwk = jwk;
    }

    /**
     * 作为客户端时的配置
     *
     * @author lym
     */
    @SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
    public class OAuth2ClientProperties {

        /**
         * 第三方应用appId
         */
        private String clientId;
        /**
         * 第三方应用appSecret
         */
        private String clientSecret;
        /**
         * 针对此应用发出的 token 的有效时间，默认 2 小时
         */
        private int accessTokenValidateSeconds = 2 * (int) ChronoUnit.HOURS.getDuration().getSeconds();


        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public int getAccessTokenValidateSeconds() {
            return accessTokenValidateSeconds;
        }

        public void setAccessTokenValidateSeconds(int accessTokenValidateSeconds) {
            this.accessTokenValidateSeconds = accessTokenValidateSeconds;
        }

    }
}
