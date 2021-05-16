package org.shoulder.autoconfigure.operation;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.ServletUtil;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.enums.TerminalType;
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
        ShoulderCurrentUserOperator operator;
        if (StringUtils.isEmpty(userId)) {
            operator = new ShoulderCurrentUserOperator(SystemOperator.getInstance());
        } else {
            operator = new ShoulderCurrentUserOperator(AppContext.getUserId());
        }
        operator.setRemoteAddress(ServletUtil.getRemoteAddress());
        operator.setTerminalId(ServletUtil.getSession().getId());
        operator.setTerminalType(TerminalType.BROWSER);
        operator.setTerminalInfo(ServletUtil.getUserAgent());
        return operator;
    }
}
