package org.shoulder.crypto.aes;

/**
 * 非对称的加解密以及签名工具实现。
 * 加解密实现为 SymmetricCryptoProcessor，本类做字符串与 byte[] 的转换。
 * 同时支持默认密钥，与多密钥对
 *
 * @author lym
 */
/*
public class DefaultSymmetricTextCipher implements SymmetricTextCipher {

    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;

    private static final Logger log = LoggerFactory.getLogger(DefaultSymmetricTextCipher.class);

    // 非对称加密处理器
    private final SymmetricCryptoProcessor processor;

    public DefaultSymmetricTextCipher(SymmetricCryptoProcessor processor) {
        this.processor = processor;
    }

    @Override
    public String decrypt(String cipher) throws CipherRuntimeException {
        return processor.decrypt(key, iv, cipher);
    }

    @Override
    public byte[] decryptAsBytes(String cipher) throws CipherRuntimeException {
        return processor.decrypt(key, iv, cipher);
    }

    @Override
    public String encrypt(String text) throws CipherRuntimeException {
        return processor.encrypt(key, iv, text);
    }

}
*/
