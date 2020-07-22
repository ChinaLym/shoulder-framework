package org.shoulder.crypto.negotiation.annotation.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;

import java.io.IOException;

/**
 * 请求方加密（序列化）处理
 *
 * @author lym
 */
@Slf4j
public class RequestEncryptStdSerializer extends StdSerializer<String> {

    private static final long serialVersionUID = 1L;

    protected RequestEncryptStdSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        TransportCipher cipher = TransportCipherHolder.getRequestCipher();
        String cipherText = "";
        try {
            cipherText = cipher.encrypt(value);
        } catch (AesCryptoException e) {
            log.info("Client encrypt exception.", e);
        }
        log.info("Client encrypt, cipherText is {}.", cipherText);
        //TransportCipherHolder.remove
        gen.writeString(cipherText);
    }

}
