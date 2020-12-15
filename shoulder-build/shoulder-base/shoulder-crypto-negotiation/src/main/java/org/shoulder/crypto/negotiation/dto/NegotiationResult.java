package org.shoulder.crypto.negotiation.dto;

import java.io.Serializable;

/**
 * 协商完成后用于缓存的数据
 *
 * @author lym
 */
public class NegotiationResult implements Serializable {

    /**
     * 对方的公钥
     */
    private byte[] publicKey;

    /**
     * 协商标识
     */
    private String xSessionId;

    /**
     * 协商结果key，会话双方的共享密钥
     */
    private byte[] shareKey;

    /**
     * 协商结果向量
     */
    private byte[] localIv;

    /**
     * 本次会话中，报文的加密方式
     * 一般为对称加密算法，如 AES-256
     */
    private String encryptionScheme;

    /**
     * 协商结果key长度 256/8=32
     * @deprecated 考虑使用 encryptionScheme 替代
     */
    private int keyLength;

    /**
     * 过期的时间点
     */
    private long expireTime;

    public NegotiationResult() {
    }

    public NegotiationResult(String xSessionId, byte[] shareKey, byte[] localIv, int keyLength, int expireTime) {
        this.xSessionId = xSessionId;

        setShareKey(shareKey);
        setLocalIv(localIv);

        this.expireTime = expireTime;
        this.keyLength = keyLength;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getxSessionId() {
        return xSessionId;
    }

    public void setxSessionId(String xSessionId) {
        this.xSessionId = xSessionId;
    }

    public byte[] getShareKey() {
        return shareKey == null ? null : shareKey.clone();
    }

    public void setShareKey(byte[] shareKey) {
        this.shareKey = shareKey == null ? null : shareKey.clone();
    }

    public byte[] getLocalIv() {
        return localIv == null ? null : localIv.clone();
    }

    public void setLocalIv(byte[] localIv) {
        this.localIv = localIv == null ? null : localIv.clone();
    }

    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    public void setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
