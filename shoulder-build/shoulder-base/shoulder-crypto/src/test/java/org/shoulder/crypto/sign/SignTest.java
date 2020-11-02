package org.shoulder.crypto.sign;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

/**
 * 签名工具类测试
 */
public class SignTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 测试签名和验签正确
     */
    @Test
    public void testSignAndVerify() throws Exception {
        String key = "123456";
        String data = "123456";

        String sign = SignUtil.sign(key, data);
        log.debug(sign);
        Assertions.assertThat(SignUtil.verify(sign, key, 0)).isTrue();
    }

}
