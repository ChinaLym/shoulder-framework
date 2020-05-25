package org.shoulder.autoconfigure.log.operation;

import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.dto.SystemOperator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 操作日志的拦截器：将当前单点登录的用户作为操作日志的默认操作者
 *
 * @author lym
 */
public class OperationLogSsoOperatorInfoInterceptor extends OperationLogOperatorInfoInterceptor {


    /**
     * 用当前登录的用户信息作为操作日志中操作者信息默认值
     * todo 解析当前登录用户信息，如从 spring security context holder 中拿
     */
    @Override
    Operator resolveOperator(HttpServletRequest request) {
        //HttpSession session = request.getSession(false);
        return SystemOperator.getInstance();
    }
}
