package org.shoulder.http.exception;

import org.shoulder.core.exception.BaseRuntimeException;

/**
 * 解析失败
 *
 * @author lym
 */
public class ResolveFailException extends BaseRuntimeException {

    private static final long serialVersionUID = -2745556685325930983L;

    public ResolveFailException(Throwable cause) {
        super(cause);
    }
}
