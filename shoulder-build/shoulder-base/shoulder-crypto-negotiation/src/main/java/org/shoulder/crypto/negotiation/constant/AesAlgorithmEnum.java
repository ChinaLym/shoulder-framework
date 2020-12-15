package org.shoulder.crypto.negotiation.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * aes 安全算法枚举，用于密钥协商阶段算法选择
 *
 * @author lym
 */
public enum AesAlgorithmEnum {

    /**
     * 支持的 aes 算法
     */
    AES_128_CBC("AES/CBC/PKCS5Padding", 128),
    AES_128_ECB("AES/ECB/PKCS5Padding", 128),
    AES_128_CBC_NoPadding("AES/CBC/NoPadding", 128),
    AES_128_ECB_NoPadding("AES/ECB/NoPadding", 128),


    AES_192_CBC("AES/CBC/PKCS5Padding", 192),
    AES_192_ECB("AES/ECB/PKCS5Padding", 192),
    AES_192_CBC_NoPadding("AES/CBC/NoPadding", 192),
    AES_192_ECB_NoPadding("AES/ECB/NoPadding", 192),


    AES_256_CBC("AES/CBC/PKCS5Padding", 256),
    AES_256_ECB("AES/ECB/PKCS5Padding", 256),
    AES_256_CBC_NoPadding("AES/CBC/NoPadding", 256),
    AES_256_ECB_NoPadding("AES/ECB/NoPadding", 256);

    /**
     * 加密算法名称
     */
    private String algorithmName;

    /**
     * 密钥长度
     */
    private int keyLength;

    AesAlgorithmEnum(String algorithmName, int keyLength) {
        this.algorithmName = algorithmName;
        this.keyLength = keyLength;
    }

    public static Set<String> getSupportAesAlgorithm() {
        Set<String> algorithms = new HashSet<>();
        for (AesAlgorithmEnum algorithmEnum : AesAlgorithmEnum.values()) {
            algorithms.add(algorithmEnum.algorithmName);
        }
        return algorithms;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getKeyLength() {
        return keyLength;
    }

    @Override
    public String toString() {
        return algorithmName + keyLength;
    }
}
