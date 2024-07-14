package org.shoulder.crypto.endpoint;

import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加解密开放端点
 *
 * @author lym
 */
@RestController
public class CryptoEndpoint {

    //private final String xDataKeyInHeader = "xDataKey";

    /**
     * 非对称加密，用于供客户端获取公钥，并加密传输数据密钥
     */
    private final AsymmetricTextCipher asymmetricTextCipher;

    /**
     * 对称加密，用于解密敏感数据
     */
    //private final SymmetricCipher symmetricCipher;

    public CryptoEndpoint(AsymmetricTextCipher asymmetricTextCipher){ //, SymmetricCipher symmetricCipher) {
        this.asymmetricTextCipher = asymmetricTextCipher;
        //this.symmetricCipher = symmetricCipher;// DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName())
    }

    /**
     * 前端请求公钥
     *
     * @return 返回公钥
     */
    @RequestMapping(value = "${shoulder.crypto.asymmetric.endpoint.path:/api/v1/crypto/publicKey}", method = {RequestMethod.GET, RequestMethod.POST})
    public String getPublicKey(@RequestParam(required = false) String keyPairId) throws KeyPairException {
        if(StringUtils.isBlank(keyPairId)) {
            // 不传使用默认的
            return asymmetricTextCipher.getPublicKey();
        }
        // 使用具体的
        return asymmetricTextCipher.getPublicKey(keyPairId);
    }

    //public byte[] decryptContent(String xDataKey, String xiv, byte[] cipherContent) {
    //    try {
    //        byte[] dk = asymmetricTextCipher.decryptAsBytes(getCurrentKeyPairId(), xDataKey);
    //        byte[] iv = ByteSpecification.decodeToBytes(xiv);
    //        return symmetricCipher.decrypt(dk, iv, cipherContent);
    //    } catch (CryptoException e) {
    //        throw new BaseRuntimeException(e);
    //    }
    //}


    //protected String getCurrentKeyPairId() {
    //    String currentUserId = AppContext.getUserId();
    //    return StringUtils.isEmpty(currentUserId) ? "" :
    //        Md5Crypt.md5Crypt(currentUserId.getBytes(AppInfo.charset()))
    //            .substring(0, 3);
    //}

}
