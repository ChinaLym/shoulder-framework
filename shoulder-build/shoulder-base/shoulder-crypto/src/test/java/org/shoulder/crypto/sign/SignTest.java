package org.shoulder.crypto.sign;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;

@Slf4j
public class SignTest {

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
