package org.shoulder.crypto.negotiation.algorithm;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.AsymmetricCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;

/**
 * 基于代理的，面向协商-非对称加解密工具实现
 *
 * @author lym
 */
public class DelegateNegotiationAsymmetricCipher implements NegotiationAsymmetricCipher, ByteSpecification {

    private final AsymmetricCipher delegate;

    public DelegateNegotiationAsymmetricCipher(AsymmetricCipher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void buildKeyPair(String id, Duration ttl) throws KeyPairException {
        delegate.buildKeyPair(id, ttl);
    }

    @Override
    public void buildKeyPair(String id) throws KeyPairException {
        delegate.buildKeyPair(id);
    }

    @Override
    public byte[] decrypt(String id, byte[] content) throws AsymmetricCryptoException {
        return delegate.decrypt(id, content);
    }

    @Override
    public byte[] encrypt(String id, byte[] content) throws AsymmetricCryptoException {
        return delegate.encrypt(id, content);
    }

    @Override
    public byte[] encrypt(byte[] publicKey, byte[] content) throws AsymmetricCryptoException {
        return delegate.encrypt(publicKey, content);
    }

    @Override
    public byte[] sign(String id, byte[] content) throws AsymmetricCryptoException {
        return delegate.sign(id, content);
    }

    @Override
    public boolean verify(String id, byte[] content, byte[] signature) throws AsymmetricCryptoException {
        return delegate.verify(id, content, signature);
    }

    @Override
    public boolean verify(byte[] publicKey, byte[] content, byte[] signature) throws AsymmetricCryptoException {
        return delegate.verify(publicKey, content, signature);
    }


    @Override
    public KeyPair getKeyPair(String id) throws KeyPairException {
        return delegate.getKeyPair(id);
    }

    @Override
    public PublicKey getPublicKey(String id) throws KeyPairException {
        return delegate.getPublicKey(id);
    }


    @Override
    public String getPublicKeyString(String id) throws KeyPairException {
        return delegate.getPublicKeyString(id);
    }

    @Override
    public PrivateKey getPrivateKey(String id) throws KeyPairException {
        return delegate.getPrivateKey(id);
    }

}
