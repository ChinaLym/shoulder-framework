package com.example.demo1.controller.crypto.asymmetric;

import com.example.demo1.dto.CryptoTestDTO;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.MultiKeyPairAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.SingleKeyPairAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ECC 非对称加解密
 * <p>
 * 非对称加解密 shoulder 默认使用 ECC 256 算法，椭圆曲线采取美国安全局公布的标准椭圆曲线 NIST SPS800-57
 * 美国国家标准与技术研究院 Special Publications 800 https://csrc.nist.gov/publications/sp
 * 加解密技术指南：http://doc.itlym.cn/specs/security/crypto.html#%E7%AE%97%E6%B3%95%E7%9A%84%E9%80%89%E6%8B%A9
 *
 * @author lym
 * @see DefaultAsymmetricCryptoProcessor#ecc256
 * @see SingleKeyPairAsymmetricTextCipher
 * @see MultiKeyPairAsymmetricTextCipher
 * @see AsymmetricTextCipher
 */
@Slf4j
@SkipResponseWrap
@RestController
@RequestMapping("crypto/ecc")
public class EccCryptoDemoController {

    /**
     * 非对称加解密 shoulder 默认自动激活 ECC 256 算法
     */
    @Autowired
    private AsymmetricTextCipher asymmetricTextCipher;

    /**
     * 非对称加密  <a href="http://localhost:8080/crypto/ecc/crypto?text=123456"/>
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
     * 签名验签  <a href="http://localhost:8080/crypto/ecc/sign?text=123456"/>
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
