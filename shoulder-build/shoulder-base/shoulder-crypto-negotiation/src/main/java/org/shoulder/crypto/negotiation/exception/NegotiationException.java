package org.shoulder.crypto.negotiation.exception;

import org.shoulder.crypto.exception.CryptoException;

/**
 * 密钥协商出错
 *
 * @author lym
 */
public class NegotiationException extends CryptoException {

    public NegotiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NegotiationException(String message) {
        super(message);
    }
}
