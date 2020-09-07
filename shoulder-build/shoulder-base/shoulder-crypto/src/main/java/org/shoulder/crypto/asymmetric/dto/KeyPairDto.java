package org.shoulder.crypto.asymmetric.dto;

import org.shoulder.core.constant.ByteSpecification;

import java.security.KeyPair;

/**
 * 非对称加密的 密钥对，主要用于外部（redis）存储
 *
 * @author lym
 */
public class KeyPairDto {

    /**
     * 公钥
     */
    private String pk;

    /**
     * 私钥
     */
    private String vk;

    public KeyPairDto() {
    }

    public KeyPairDto(KeyPair keyPair) {
        pk = ByteSpecification.encodeToString(keyPair.getPublic().getEncoded());
        vk = ByteSpecification.encodeToString(keyPair.getPrivate().getEncoded());
    }

    public KeyPairDto(String pk, String vk) {
        this.pk = pk;
        this.vk = vk;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getVk() {
        return vk;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }
}
