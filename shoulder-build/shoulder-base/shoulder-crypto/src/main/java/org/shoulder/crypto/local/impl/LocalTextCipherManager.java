package org.shoulder.crypto.local.impl;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 支持多版本并存的本地存储方案（无感知切换加密方案）
 *
 * @author lym
 */
public class LocalTextCipherManager implements LocalTextCipher {

    /**
     * 实际加解密器
     * 加密选用第一个加密，解密则按照顺序遍历
     */
    private List<JudgeAbleLocalTextCipher> ciphers;

    public LocalTextCipherManager(JudgeAbleLocalTextCipher mainCipher) {
        this(Collections.singletonList(mainCipher));
    }

    public LocalTextCipherManager(List<JudgeAbleLocalTextCipher> ciphers) {
        if (CollectionUtils.isEmpty(ciphers)) {
            return;
        }
        this.ciphers = new LinkedList<>(ciphers);
        sortCiphers();
    }

    @Override
    public String encrypt(@NonNull String text) throws SymmetricCryptoException {
        return ciphers.get(0).encrypt(text);
    }

    @Override
    public String decrypt(@NonNull String cipherText) throws SymmetricCryptoException {
        // 遍历所有加密器，这里认为每个加密器是互斥的，因为一般来说 a 加密器无法解密由 b 加密器加密过的数据，因此直接交给第一个能解密的加密器进行解密
        for (JudgeAbleLocalTextCipher cipher : ciphers) {
            if (cipher.support(cipherText)) {
                return cipher.decrypt(cipherText);
            }
        }
        throw new IllegalStateException("None localCryptoCiphers can decrypt the cipherText!");
    }

    @Override
    public void ensureEncryption() {
        ciphers.forEach(JudgeAbleLocalTextCipher::ensureEncryption);
    }

    public void addCipher(JudgeAbleLocalTextCipher localTextCipher) {
        this.ciphers.add(localTextCipher);
    }

    /**
     * 是否可用，可以在启动时检查一下
     *
     * @return 是否可用
     */
    public boolean isPrepared() {
        return CollectionUtils.isEmpty(ciphers);
    }

    private void sortCiphers() {
        this.ciphers.sort(Comparator.comparingInt(Ordered::getOrder));
    }

}
