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
 * 客户端解密序列化处理
 * 用在请求发起端
 *
 * @author lym
 */
@Slf4j
public class ResponseDecryptStdDeserializer extends StdDeserializer<String> {

    private static final long serialVersionUID = 1L;

    protected ResponseDecryptStdDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String cipherText = p.getText();
        // 不是base64编码后的格式，表示非敏感信息，即不需要解密处理，直接返回即可。
        if (StringUtils.isNotBase64(cipherText)) {
            return cipherText;
        }
        log.debug("Client response transport decrypt, cipherText is {}.", cipherText);
        TransportCipher cipher = TransportCipherHolder.getResponseHandler();
        String data = "";
        try {
            data = cipher.decrypt(cipherText);
        } catch (AesCryptoException e) {
            log.error("Client response transport decrypt FAIL!", e);
        }
        //TransportCipherHolder.remove
        return data;
    }

}
