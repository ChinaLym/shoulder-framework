package org.shoulder.crypto.digest;

/**
 * 信息摘要能力接口
 *
 * @author lym
 */
public interface DigestAble {

    /**
     * 离散
     *
     * @param bytes 待离散的数组
     * @return 摘要
     */
    byte[] digest(byte[] bytes);

    /**
     * 校验 cipher 的明文是否为 text
     *
     * @param text
     * @param cipher
     * @return
     */
    boolean veritfy(byte[] text, byte[] cipher);
}