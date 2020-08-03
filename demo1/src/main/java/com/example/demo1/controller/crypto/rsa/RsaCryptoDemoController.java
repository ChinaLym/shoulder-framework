package com.example.demo1.controller.crypto.rsa;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可以自动将日期转为对应的类
 *
 * @author lym
 */
@Slf4j
@SkipResponseWrap
@RestController
@RequestMapping("crypto/rsa")
public class RsaCryptoDemoController {


    @Autowired
    private LocalTextCipher localTextCipher;

    /**
     * 本地加密  <a href="http://localhost:8080/crypto/local/encrypt?text=123456"/>
     */
    @GetMapping("encrypt")
    public String localEncrypt(String text) throws SymmetricCryptoException {
        String cipher = localTextCipher.encrypt(text);
        log.info("text({}) encrypt result: {}", text, cipher);
        return cipher;
    }

    /**
     * 本地解密  <a href="http://localhost:8080/crypto/local/encrypt?cipher="/>
     * todo 将 <a href="http://localhost:8080/crypto/local/encrypt?text=123456"/> 接口返回值放入，才能正确调用该接口，否则将报错
     */
    @GetMapping("decrypt")
    public String localDecrypt(String cipher) throws SymmetricCryptoException {
        String text = localTextCipher.decrypt(cipher);
        log.info("cipher({}) decrypt result: {}", cipher, text);
        return text;
    }

}
