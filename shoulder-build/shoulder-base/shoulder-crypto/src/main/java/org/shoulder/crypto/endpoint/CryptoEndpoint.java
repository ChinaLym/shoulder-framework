package org.shoulder.crypto.endpoint;

import org.apache.commons.codec.digest.Md5Crypt;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.aes.SymmetricCryptoUtils;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.exception.CryptoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加解密开放端点
 *
 * @author lym
 */
@RestController
public class CryptoEndpoint {

    private final String xDataKeyInHeader = "xDataKey";

    @Autowired
    private AsymmetricTextCipher asymmetricTextCipher;


    /**
     * 前端请求公钥
     *
     * @return 返回公钥
     */
    @RequestMapping(value = "/publicKey", method = RequestMethod.GET)
    public String getPublicKey() throws KeyPairException {
        return asymmetricTextCipher.getPublicKey(getCurrentKeyPairId());
    }

    public byte[] decryptContent(String xDataKey, String xiv, byte[] content) {
        try {
            byte[] dk = asymmetricTextCipher.decryptAsBytes(getCurrentKeyPairId(), xDataKey);
            byte[] iv = ByteSpecification.decodeToBytes(xiv);
            return SymmetricCryptoUtils.decrypt(content, dk, iv);
        } catch (CryptoException e) {
            throw new BaseRuntimeException(e);
        }
    }


    protected String getCurrentKeyPairId() {
        String currentUserId = AppContext.getUserIdAsString();
        return StringUtils.isEmpty(currentUserId) ? "" :
            Md5Crypt.md5Crypt(currentUserId.getBytes(AppInfo.charset()))
                .substring(0, 3);
    }

}
