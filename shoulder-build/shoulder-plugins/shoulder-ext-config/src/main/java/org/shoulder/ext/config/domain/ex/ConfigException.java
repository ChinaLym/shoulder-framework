package org.shoulder.ext.config.domain.ex;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.ErrorCode;

/**
 * 业务异常
 *
 * @author lym
 */
public class ConfigException extends BaseRuntimeException {

    @Serial private static final long serialVersionUID = 6894693369570821029L;

    /**
     * Constructor.
     *
     * @param errorCode the error code
     */
    public ConfigException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructor.
     *
     * @param cause     the cause
     * @param errorCode the error code
     */
    public ConfigException(Throwable cause, ErrorCode errorCode) {
        super(errorCode.getMessage(), cause);
    }

}
