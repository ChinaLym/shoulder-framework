package org.shoulder.data.sequence.exceptions;

import java.io.Serial;

/**
 *
 * @author lym
 *
 */
public class SequenceException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;

    public SequenceException() {
        super();
    }

    public SequenceException(String message) {
        super(message);
    }

    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceException(Throwable cause) {
        super(cause);
    }
}
