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
 * server端返回值加密（序列化）处理
 *
 * @author lym
 */
@Slf4j
public class ResponseEncryptStdSerializer extends StdSerializer<String> {

    private static final long serialVersionUID = 1L;

    protected ResponseEncryptStdSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        TransportCipher adapter = TransportCipherHolder.getResponseCipher();
        String cipherText = "";
        try {
            cipherText = adapter.encrypt(value);
        } catch (AesCryptoException e) {
            log.error("Server response transport encrypt FAIL!", e);
        }
        log.info("Server response encrypt, cipherText is {}.", cipherText);
        //TransportCipherHolder.remove
        gen.writeString(cipherText);
    }

}
