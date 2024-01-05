package org.shoulder.crypto.negotiation.cipher;

import jakarta.annotation.Nonnull;
import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.exception.CipherRuntimeException;

/**
 * 传输加解密：仅用于传输
 * 子类职责：负责实现传输加解密
 * todo 添加 getDataKey
 *
 * @author lym
 */
public interface TransportTextCipher extends TextCipher {

    /**
     * 加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     * @throws CipherRuntimeException aes异常
     */
    @Override
    String encrypt(@Nonnull String text) throws CipherRuntimeException;

    /**
     * 解密
     *
     * @param cipherText 密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     * @throws CipherRuntimeException aes异常
     */
    @Override
    String decrypt(@Nonnull String cipherText) throws CipherRuntimeException;

    /**
     * 加密或解密，根据实现类具体职责决定
     *
     * @param input input
     * @return 处理完的
     */
    default String doCipher(String input) {
        throw new UnsupportedOperationException("not support!");
    }

}
