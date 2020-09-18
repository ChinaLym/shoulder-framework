package org.shoulder.crypto.endpoint;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
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

    @Autowired
    private AsymmetricTextCipher asymmetricTextCipher;


    /**
     * 前端请求公钥
     *
     * @return 返回公钥
     */
    //@ApiOperation(value = "前端请求公钥", notes = "")
    @RequestMapping(value = "/publicKey", method = RequestMethod.GET)
    public BaseResponse<String> getPublicKey() throws KeyPairException {
        return BaseResponse.success(asymmetricTextCipher.getPublicKey());
    }

}
