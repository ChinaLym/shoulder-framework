package org.shoulder.crypto.negotiation.cache.dto;

import java.io.Serializable;

/**
 * 协商完成后用于缓存的数据
 * @author lym
 */
public class KeyExchangeResult implements Serializable {

    /** 对方的公钥 */
    private byte[] publicKey;

    /** 协商标识 */
    private String xSessionId;

    /** 协商结果key，会话双方的共享密钥 */
    private byte[] localKey;

    /** 协商结果向量 */
    private byte[] localIv;

    /** 协商结果key长度 256/8=32 */
    private int keyLength;

    /** 过期的时间点 */
    private long expireTime;

    public KeyExchangeResult() {
    }

    public KeyExchangeResult(String xSessionId, byte[] localKey, byte[] localIv,int keyLength, int expireTime) {
        this.xSessionId = xSessionId;

        setLocalKey(localKey);
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

    public byte[] getLocalKey() {
        return localKey == null ? null : localKey.clone();
    }

    public void setLocalKey(byte[] localKey) {
        this.localKey = localKey == null ? null : localKey.clone();
    }

    public byte[] getLocalIv() {
        return localIv == null ? null : localIv.clone();
    }

    public void setLocalIv(byte[] localIv) {
        this.localIv = localIv == null ? null : localIv.clone();
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
