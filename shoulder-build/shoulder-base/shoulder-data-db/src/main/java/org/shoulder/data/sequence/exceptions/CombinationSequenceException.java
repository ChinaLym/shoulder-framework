package org.shoulder.data.sequence.exceptions;

import org.springframework.dao.DataAccessException;

import java.io.Serial;

/**
 *
 * @author lym
 */
public class CombinationSequenceException extends DataAccessException {

    /**  */
    @Serial private static final long serialVersionUID = -1L;

    public CombinationSequenceException(String message) {
        super(message);
    }

    public CombinationSequenceException(String message, Exception e) {
        super(message, e);
    }

    public CombinationSequenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
