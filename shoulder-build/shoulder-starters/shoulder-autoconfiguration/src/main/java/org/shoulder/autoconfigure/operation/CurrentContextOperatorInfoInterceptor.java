package org.shoulder.autoconfigure.operation;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.model.Operator;
import org.shoulder.log.operation.model.ShoulderCurrentUserOperator;
import org.shoulder.log.operation.model.SystemOperator;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志的拦截器：将当前单点登录的用户作为操作日志的默认操作者
 *
 * @author lym
 */
public class CurrentContextOperatorInfoInterceptor extends OperationLogOperatorInfoInterceptor {

    /**
     * 用当前登录的用户信息作为操作日志中操作者信息默认值
     * 也可覆盖，改为从 spring security context holder 中拿
     */
    @Override
    protected Operator resolveOperator(HttpServletRequest request) {
        String userId = AppContext.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return SystemOperator.getInstance();
        }
        ShoulderCurrentUserOperator operator = new ShoulderCurrentUserOperator(AppContext.getUserId());
        operator.setTerminalInfo(String.valueOf(AppContext.getLocale()));
        return operator;
    }
}
