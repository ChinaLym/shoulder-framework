package com.example.demo1.crypto;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Test;

public class CryptoTest extends BaseWebTest {


    @Test
    public void rsaTestCrypto() throws Exception {
        doGetTest("/crypto/rsa/crypto?text=123456",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"text\":\"123456\",\"cipher\":\"");
        doGetTest("/crypto/rsa/sign?text=123456",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"\"}");
    }

    @Test
    public void eccTestCrypto() throws Exception {
        doGetTest("/crypto/ecc/crypto?text=123456",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"text\":\"123456\",\"cipher\":\"");
        doGetTest("/crypto/ecc/sign?text=123456",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"\"}");
    }

    @Test
    public void localTestCrypto() throws Exception {
        doGetTest("/crypto/local/crypto?text=123456",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":{\"text\":\"123456\",\"cipher\":\"${a8}");
    }


}
