package org.shoulder.crypto.symmetric;

import java.util.HashSet;
import java.util.Set;

/**
 * 对称加密安全算法枚举，用于密钥协商阶段算法选择
 * 16字节(128bit)的固定块大小
 * PKCS5/7  cipherLen = (clearLen/16 + 1) * 16;
 * 如果明文是块大小的倍数，则需要一个全新的块进行填充。 如明文为16个字节。 密文将占用32个字节。
 * 使用密文存储IV（初始向量）需要为IV增加密文大小16个字节。
 *
 * @author lym
 */
public enum SymmetricAlgorithmEnum {


    // ------------------------- des 过时，认为不安全，暂不列出 -------------------------


    // ------------------------- aes 最常用 CBC Padding。推荐一般 CBC，流则 GCM -------------------------

    // sun-JVM 支持的( SunPKCS11 JsseJce )
    AES_CBC_PKCS5Padding("AES/CBC/PKCS5Padding"),
    AES_ECB_PKCS5Padding("AES/ECB/PKCS5Padding"),

    /**
     * 单数据块短报文勉强使用，但不建议
     */
    AES_ECB("AES/ECB/NoPadding"),
    /**
     * 最常用，一般文本，无法并行加密，可并行解密【推荐】
     */
    AES_CBC("AES/CBC/NoPadding"),
    AES_CFB("AES/CFB/NoPadding"),
    AES_OFB("AES/OFB/NoPadding"),

    /**
     * 流式
     */
    AES_CTR("AES/CTR/NoPadding"),
    /**
     * CTR模式和GHASH的组合【推荐】
     */
    AES_GCM("AES/GCM/NoPadding"),
    /**
     * CBC-MAC 的简单组合，加密非常慢
     */
    AES_CCM("AES/CCM/NoPadding"),

    AES_CTS("AES/CTS/NoPadding"),
    AES_SIC("AES/SIC/NoPadding"),
    AES_GCTR("AES/GCTR/NoPadding"),
    AES_PGPCFB("AES/PGPCFB/NoPadding"),
    AES_OpenPGPCFB("AES/OpenPGPCFB/NoPadding"),

    // OCB 最好，且无需强随机iv，但有专利在美国，可用于自由软件，不能用于军事，未列出。
    // XEX，XTS，LRW 这些仅仅是适用于磁盘的，适合随机数据加密（非流），PITA

    // ------------------------- sm4 与 AES 算法基本一致 -------------------------


    SM4_CBC("SM4/CBC/NoPadding"),
    SM4_ECB("SM4/ECB/NoPadding"),
    SM4_CCM("SM4/CCM/NoPadding"),
    SM4_CTR("SM4/CTR/NoPadding"),
    SM4_GCM("SM4/GCM/NoPadding"),
    SM4_CTS("SM4/CTS/NoPadding"),
    SM4_SIC("SM4/SIC/NoPadding"),
    SM4_CFB("SM4/CFB/NoPadding"),
    SM4_OFB("SM4/OFB/NoPadding"),
    SM4_GCTR("SM4/GCTR/NoPadding"),
    SM4_PGPCFB("SM4/PGPCFB/NoPadding"),
    SM4_OpenPGPCFB("SM4/OpenPGPCFB/NoPadding"),

    SM4_CBC_PKCS5Padding("SM4/CBC/PKCS5Padding"),
    SM4_ECB_PKCS5Padding("SM4/ECB/PKCS5Padding"),


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
