package com.example.demo1.controller.crypto.asymmetric;

import com.example.demo1.config.CryptoConfig;
import com.example.demo1.dto.CryptoTestDTO;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RSA 非对称加解密  若需测试 rsa 需要打开 {@link CryptoConfig#rsa2048} 的 @Bean
 * 否则 shoulder 默认使用性能更好，安全系数更高的 ECC 算法，{@link EccCryptoDemoController}
 *
 * @author lym
 * @see DefaultAsymmetricCipher#ecc256
 */
@Slf4j
@RestController
@RequestMapping("crypto/rsa")
public class RsaCryptoDemoController {

    /**
     * 要测试先读类注释，否则 shoulder 默认使用性能更好，安全系数更高的 ECC 算法，{@link EccCryptoDemoController}
     */
    @Autowired
    private AsymmetricTextCipher asymmetricTextCipher;

    /**
     * 非对称加密  <a href="http://localhost:8080/crypto/rsa/crypto?text=123456"/>
     */
    @GetMapping("crypto")
    public CryptoTestDTO testCrypto(String text) throws AsymmetricCryptoException {
        // 2. 加密
        String cipher = asymmetricTextCipher.encrypt(text);
        log.info("text({}) encrypt result: {}", text, cipher);
        // 3. 解密
        String decryptText = asymmetricTextCipher.decrypt(cipher);
        log.info("cipher({}) decrypt result: {}", cipher, decryptText);
        return new CryptoTestDTO(text, cipher, decryptText);
    }

    /**
     * 签名验签  <a href="http://localhost:8080/crypto/rsa/sign?text=123456"/>
     */
    @GetMapping("sign")
    public String testSign(String text) throws AsymmetricCryptoException {
        // 2. 验签
        String sign = asymmetricTextCipher.sign(text);
        log.info("text({}) sign result: {}", text, sign);
        // 3. 验签
        log.info("sign({}) verify result: {}", sign, asymmetricTextCipher.verify(text, sign));
        return "";
    }

}
