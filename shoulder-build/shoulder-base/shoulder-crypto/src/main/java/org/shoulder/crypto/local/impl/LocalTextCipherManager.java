package org.shoulder.crypto.local.impl;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.core.Ordered;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    private final List<JudgeAbleLocalTextCipher> ciphers;

    public LocalTextCipherManager(@Nonnull JudgeAbleLocalTextCipher mainCipher) {
        this(Collections.singletonList(mainCipher));
    }

    public LocalTextCipherManager(@Nonnull List<JudgeAbleLocalTextCipher> ciphers) {
        this.ciphers = ciphers.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .toList();
    }

    @Override
    public String encrypt(@Nonnull String text) {
        return ciphers.get(0).encrypt(text);
    }

    @Override
    public String decrypt(@Nonnull String cipherText) {
        // 遍历所有加密器，这里认为每个加密器是互斥的，因为一般来说 a 加密器无法解密由 b 加密器加密过的数据，因此直接交给第一个能解密的加密器进行解密
        AssertUtils.notEmpty(cipherText, CommonErrorCodeEnum.ILLEGAL_PARAM);
        return ciphers.stream()
                .filter(c -> c.support(cipherText))
                .findFirst()
                .map(c -> c.decrypt(cipherText))
                .orElseThrow(() -> new InvalidParameterException("None localCryptoCiphers can decrypt the cipherText!"));
    }

    @Override
    public void ensureInit() {
        AssertUtils.notEmpty(ciphers, CommonErrorCodeEnum.ILLEGAL_STATUS);
        ciphers.forEach(JudgeAbleLocalTextCipher::ensureInit);
    }

}
