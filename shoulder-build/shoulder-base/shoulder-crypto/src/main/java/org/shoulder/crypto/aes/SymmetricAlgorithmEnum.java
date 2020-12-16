package org.shoulder.crypto.aes;

import java.util.HashSet;
import java.util.Set;

/**
 * 对称加密安全算法枚举，用于密钥协商阶段算法选择
 *
 * @author lym
 */
public enum SymmetricAlgorithmEnum {


    // ------------------------- des 过时，认为不安全，暂不列出 -------------------------


    // ------------------------- aes 最常用 CBC Padding -------------------------

    // sun-JVM 支持的( SunPKCS11 JsseJce )
    AES_CBC_PKCS5Padding("AES/CBC/PKCS5Padding"),
    AES_ECB_PKCS5Padding("AES/ECB/PKCS5Padding"),

    AES_CBC("AES/CBC/NoPadding"),
    AES_ECB("AES/ECB/NoPadding"),
    AES_CCM("AES/CCM/NoPadding"),
    AES_CTR("AES/CTR/NoPadding"),
    AES_GCM("AES/GCM/NoPadding"),
    AES_CTS("AES/CTS/NoPadding"),
    AES_SIC("AES/SIC/NoPadding"),
    AES_CFB("AES/CFB/NoPadding"),
    AES_OFB("AES/OFB/NoPadding"),
    AES_GCTR("AES/GCTR/NoPadding"),
    AES_PGPCFB("AES/PGPCFB/NoPadding"),
    AES_OpenPGPCFB("AES/OpenPGPCFB/NoPadding"),


    // ------------------------- sm4 与 AES 算法基本一致 -------------------------


    SM4_CBC("AES/CBC/NoPadding"),
    SM4_ECB("AES/ECB/NoPadding"),
    SM4_CCM("AES/CCM/NoPadding"),
    SM4_CTR("AES/CTR/NoPadding"),
    SM4_GCM("AES/GCM/NoPadding"),
    SM4_CTS("AES/CTS/NoPadding"),
    SM4_SIC("AES/SIC/NoPadding"),
    SM4_CFB("AES/CFB/NoPadding"),
    SM4_OFB("AES/OFB/NoPadding"),
    SM4_GCTR("AES/GCTR/NoPadding"),
    SM4_PGPCFB("AES/PGPCFB/NoPadding"),
    SM4_OpenPGPCFB("AES/OpenPGPCFB/NoPadding"),

    ;

    /**
     * 加密算法名称
     */
    private String algorithmName;

    SymmetricAlgorithmEnum(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static Set<String> getSupportAesAlgorithms() {
        Set<String> algorithms = new HashSet<>();
        for (SymmetricAlgorithmEnum algorithmEnum : SymmetricAlgorithmEnum.values()) {
            algorithms.add(algorithmEnum.algorithmName);
        }
        return algorithms;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    @Override
    public String toString() {
        return algorithmName;
    }
}
