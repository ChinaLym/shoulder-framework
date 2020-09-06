package com.example.demo1.controller.crypto.local;

import com.example.demo1.dto.CryptoTestDTO;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本地存储加解密，用于对数据库中的数据进行加密解密
 *
 * 本地：指的是 A 应用加密的数据仅 A 应用能解密，其他应用无法解密
 *
 * @see Aes256LocalTextCipher
 *
 * @author lym
 */
@Slf4j
@SkipResponseWrap
@RestController
@RequestMapping("crypto/local")
public class LocalCryptoDemoController {

    // 1. 注入 LocalTextCipher
    @Autowired
    private LocalTextCipher localTextCipher;

    /**
     * 本地加密  <a href="http://localhost:8080/crypto/local/crypto?text=123456"/>
     */
    @GetMapping("crypto")
    public CryptoTestDTO testCrypto(String text) throws SymmetricCryptoException {
        // 2. 加密
        String cipher = localTextCipher.encrypt(text);
        log.info("text({}) encrypt result: {}", text, cipher);
        // 3. 解密
        String decryptText = localTextCipher.decrypt(cipher);
        log.info("cipher({}) decrypt result: {}", cipher, decryptText);
        return new CryptoTestDTO(text, cipher, decryptText);
    }

}
