package org.shoulder.http.exception;

import org.shoulder.core.exception.BaseRuntimeException;

import java.io.Serial;

/**
 * 解析失败
 *
 * @author lym
 */
public class ResolveFailException extends BaseRuntimeException {

    @Serial
    private static final long serialVersionUID = -2745556685325930983L;

    public ResolveFailException(Throwable cause) {
        super(cause);
    }
}
