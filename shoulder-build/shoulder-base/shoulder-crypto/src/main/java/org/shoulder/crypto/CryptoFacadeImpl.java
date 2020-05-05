package org.shoulder.crypto;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import lombok.NonNull;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 加解密工具
 * @author lym
 */
@Service
public class CryptoFacadeImpl implements CryptoFacade {

    private final LocalTextCipher local;

    private final AsymmetricTextCipher asymmetric;

    @Autowired
    public CryptoFacadeImpl(LocalTextCipher local, AsymmetricTextCipher asymmetric) {
        this.local = local;
        this.asymmetric = asymmetric;
    }

    @Override
    public String encryptLocal(@NonNull String text) throws SymmetricCryptoException {
        return local.encrypt(text);
    }

    @Override
    public String decryptLocal(@NonNull String cipherText) throws SymmetricCryptoException {
        return local.encrypt(cipherText);
    }

    @Override
    public void initLocal() {
        local.ensureEncryption();
    }

    // ================================ 传输加解密（如前后交互） =====================================

    @Override
    public String getPk() throws KeyPairException {
        return asymmetric.getPublicKey();
    }

    @Override
    public String encryptAsymmetric(String text) throws AsymmetricCryptoException {
        return asymmetric.encrypt(text);
    }

    @Override
    public String encryptAsymmetric(String text, String publicKey) throws AsymmetricCryptoException {
        return asymmetric.encrypt(text, publicKey);
    }

    @Override
    public String decryptAsymmetric(String cipherText) throws AsymmetricCryptoException {
        return asymmetric.decrypt(cipherText);
    }

    @Override
    public String sign(String text) throws AsymmetricCryptoException {
        return asymmetric.sign(text);
    }

    @Override
    public boolean verify(String text, String signature) throws AsymmetricCryptoException {
        return asymmetric.verify(text, signature);
    }

}
