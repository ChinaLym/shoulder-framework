package org.shoulder.crypto.asymmetric.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.StringUtils;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;

/**
 * 非对称加密的 密钥对
 * 在内存中存储时，会通过存储 originKeyPair 避免序列化开销。需要序列化时，又通过 lazy serial 和 缓存 保证不增加额外成本
 *
 * @author lym
 */
public class KeyPairDto {

    /**
     * 保存原始的 KeyPair
     */
    @JsonIgnore
    private transient KeyPair originKeyPair;

    /**
     * 公钥
     */
    private String pk;

    /**
     * 私钥
     */
    private String vk;

    /**
     * 过期时间点，null代表不过期
     */
    private Instant expireTime;

    public KeyPairDto() {
    }

    public KeyPairDto(KeyPair keyPair) {
        this.originKeyPair = keyPair;
    }

    public KeyPairDto(KeyPair keyPair, Duration ttl) {
        this.originKeyPair = keyPair;
        this.expireTime = ttl == null ? null : Instant.now().plus(ttl);
    }

    public KeyPairDto(String pk, String vk) {
        this.pk = pk;
        this.vk = vk;
    }

    public String getPk() {
        return StringUtils.isNotEmpty(pk) ? pk
            : originKeyPair != null ? (pk = ByteSpecification.encodeToString(originKeyPair.getPublic().getEncoded()))
            : null;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getVk() {
        return StringUtils.isNotEmpty(vk) ? vk
            : originKeyPair != null ? (vk = ByteSpecification.encodeToString(originKeyPair.getPrivate().getEncoded()))
            : null;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public KeyPairDto setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @JsonIgnore
    public KeyPair getOriginKeyPair() {
        return originKeyPair;
    }

    public void setOriginKeyPair(KeyPair originKeyPair) {
        this.originKeyPair = originKeyPair;
    }
}
