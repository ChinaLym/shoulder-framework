package org.shoulder.crypto.local;

import jakarta.annotation.Nonnull;
import org.shoulder.crypto.TextCipher;

/**
 * 本地数据加密解密：只能由本应用实现解密。
 * 使用者直接使用的接口
 *
 * @author lym
 */
public interface LocalTextCipher extends TextCipher {

    /**
     * 加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     */
    @Override
    String encrypt(@Nonnull String text);

    /**
     * 以Aes256解密
     *
     * @param cipherText aes256 加密过的密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     */
    @Override
    String decrypt(@Nonnull String cipherText);

    /**
     * 确保加密功能正常使用
     * 默认懒加载，第一次加解密时初始化，若希望优化第一次加解密时的性能损耗，在项目启动后调用即可
     */
    void ensureInit();
}
