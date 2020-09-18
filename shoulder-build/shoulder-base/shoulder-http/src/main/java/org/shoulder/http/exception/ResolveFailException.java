package org.shoulder.http.exception;

import org.shoulder.core.exception.BaseRuntimeException;

/**
 * 解析失败
 *
 * @author lym
 */
public class ResolveFailException extends BaseRuntimeException {

    public ResolveFailException(Throwable cause) {
        super(cause);
    }
}
