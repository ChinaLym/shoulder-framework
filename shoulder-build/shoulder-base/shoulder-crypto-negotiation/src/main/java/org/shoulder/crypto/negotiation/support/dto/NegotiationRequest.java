package org.shoulder.crypto.negotiation.support.dto;

import java.util.Collections;
import java.util.List;

/**
 * 协商发起者提供参数
 * Token = 服务端公钥签名（xSessionId + publicKey）
 *
 * @author lym
 */
public class NegotiationRequest {

    /**
     * Header 中 会话标识
     */
    private transient String xSessionId;

    /**
     * Header 中 防篡改签名
     */
    private transient String token;

    /**
     * 发起者用于协商的公钥
     */
    private String publicKey;

    /**
     * 【非空】发起者支持的 报文 加解密算法，如 AES-128 / AES-192 / AES-256
     */
    private List<String> encryptionSchemeSupports = Collections.singletonList("AES-256");

    /**
     * 是否强制双方重新协商
     */
    private boolean refresh = false;

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

    public List<String> getEncryptionSchemeSupports() {
        return encryptionSchemeSupports;
    }

    public void setEncryptionSchemeSupports(List<String> encryptionSchemeSupports) {
        this.encryptionSchemeSupports = encryptionSchemeSupports;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }
}
