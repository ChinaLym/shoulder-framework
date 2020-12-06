package org.shoulder.crypto.negotiation.support.dto;

import org.shoulder.crypto.negotiation.constant.NegotiationConstants;

/**
 * 服务端者返回参数
 * Token = 服务端私钥签名（所有参数拼接）
 *
 * @author lym
 */
public class NegotiationResponse implements Cloneable {

    /**
     * Header 中 会话标识
     */
    private transient String xSessionId;

    /**
     * Header 中 防篡改签名
     */
    private transient String token;

    /**
     * 对方的公钥
     */
    private String publicKey;

    /**
     * 本次会话对称加密算法标识
     */
    private String aes;

    /**
     * 算法密钥字节数，注意非 bit 位数，如 256/8=32，192/8=24，128/8=16
     */
    private Integer keyBytesLength = 32;

    /**
     * 多少毫秒之后本次协商过期
     */
    private int expireTime = NegotiationConstants.EXPIRE_TIME;

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

    public Integer getKeyBytesLength() {
        return keyBytesLength;
    }

    public void setKeyBytesLength(Integer keyBytesLength) {
        this.keyBytesLength = keyBytesLength;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public NegotiationResponse clone() {
        NegotiationResponse cloned = new NegotiationResponse();
        cloned.xSessionId = this.xSessionId;
        cloned.token = this.token;
        cloned.publicKey = this.publicKey;
        cloned.aes = this.aes;
        cloned.keyBytesLength = this.keyBytesLength;
        cloned.expireTime = this.expireTime;
        return cloned;
    }

}
