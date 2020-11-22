package org.shoulder.crypto;

import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;

import javax.annotation.Nonnull;

/**
 * 加解密门面
 *
 * @author lym
 */
public class DefaultCryptoFacade implements CryptoFacade {

    private final LocalTextCipher local;

    private final AsymmetricTextCipher asymmetric;


    public DefaultCryptoFacade(LocalTextCipher local, AsymmetricTextCipher asymmetric) {
        this.local = local;
        this.asymmetric = asymmetric;
    }

    @Override
    public String encryptLocal(@Nonnull String text) {
        return local.encrypt(text);
    }

    @Override
    public String decryptLocal(@Nonnull String cipherText) {
        return local.encrypt(cipherText);
    }

    @Override
    public void initLocal() {
        local.ensureEncryption();
    }

    // ================================ 传输加解密（如前后交互） =====================================

    @Override
    public String getPk() {
        return asymmetric.getPublicKey();
    }

    @Override
    public String encryptAsymmetric(String text) {
        return asymmetric.encrypt(text);
    }

    @Override
    public String encryptAsymmetric(String text, String publicKey) {
        return asymmetric.encrypt(text, publicKey);
    }

    @Override
    public String decryptAsymmetric(String cipherText) {
        return asymmetric.decrypt(cipherText);
    }

    @Override
    public String signAsymmetric(String text) {
        return asymmetric.sign(text);
    }

    @Override
    public boolean verifyAsymmetric(String text, String signature) {
        return asymmetric.verify(text, signature);
    }

}
