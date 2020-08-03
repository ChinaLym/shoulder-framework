package org.shoulder.crypto.negotiation.annotation.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;

import java.io.IOException;

/**
 * server 端解密(反序列化)处理
 *
 * @author lym
 */
@Slf4j
public class RequestDecryptStdDeserializer extends StdDeserializer<String> {

    private static final long serialVersionUID = 1L;

    protected RequestDecryptStdDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String cipherText = p.getText();
        // 不是base64编码后的格式，表示非敏感信息，即不需要解密处理，直接返回即可。
        if (StringUtils.isNotBase64(cipherText)) {
            return cipherText;
        }
        log.info("Server transport decrypt, cipherText is {}.", cipherText);
        TransportCipher cipher = TransportCipherHolder.getRequestCipher();
        String data = "";
        try {

            data = cipher.decrypt(cipherText);
        } catch (AesCryptoException e) {
            log.info("Server transport decrypt FAIL!", e);
        }
        //TransportCipherHolder.remove
        return data;
    }

}
