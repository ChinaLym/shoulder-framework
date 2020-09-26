package org.shoulder.crypto;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.exception.CipherRuntimeException;

/**
 * String 文本加密器使用者接口
 * String 类通常是直接面向业务代码使用的，不再抛检查异常，只会抛运行时异常
 * 运行已经被封装过，不会直接向外暴露细节信息的（会在错误日志、堆栈中看到）
 *
 * @author lym
 */
public interface TextCipher extends ByteSpecification {

    /**
     * 加密
     *
     * @param text 明文
     * @return 加密后的密文
     * @throws CipherRuntimeException 加解密错误
     */
    String encrypt(String text) throws CipherRuntimeException;

    /**
     * 解密
     *
     * @param cipher 密文
     * @return 解密后的明文
     * @throws CipherRuntimeException 加解密错误
     */
    String decrypt(String cipher) throws CipherRuntimeException;

}
