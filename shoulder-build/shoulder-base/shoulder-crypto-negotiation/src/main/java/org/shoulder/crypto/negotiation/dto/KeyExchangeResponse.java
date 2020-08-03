package org.shoulder.crypto.negotiation.dto;

import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;

/**
 * 服务端者返回参数
 * Token = 服务端私钥签名（所有参数拼接）
 *
 * @author lym
 */
public class KeyExchangeResponse {

    /**
     * Header 中 会话标识
     */
    private transient String xSessionId;

    /**
     * Header 中 防篡改签名
     */
    private transient String token;

    /**
     * 服务端的公钥
     */
    private String publicKey;

    /**
     * 本次会话 aes 算法
     */
    private String aes;

    /**
     * 算法密钥长度 256/8=32
     */
    private Integer keyLength = 32;

    /**
     * 多少毫秒之后本次协商过期
     */
    private int expireTime = KeyExchangeConstants.EXPIRE_TIME;

    public String getxSessionId() {
        return xSessionId;
    }

    public void setxSessionId(String xSessionId) {
        this.xSessionId = xSessionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAes() {
        return aes;
    }

    public void setAes(String aes) {
        this.aes = aes;
    }

    public Integer getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
}
