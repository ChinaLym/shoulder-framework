package org.shoulder.autoconfigure.operation;

import org.shoulder.core.context.AppContext;
import org.shoulder.log.operation.enums.TerminalType;
import org.shoulder.log.operation.model.Operator;
import org.shoulder.log.operation.model.ShoulderCurrentUserOperator;
import org.shoulder.log.operation.model.SystemOperator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志的拦截器：根据 spring security 的上下文中的认证凭证解析用户，并作为当前上下文 操作日志的默认操作者
 *
 * @author lym
 */
public class SpringSecurityOperatorInfoInterceptor extends OperationLogOperatorInfoInterceptor {

    /**
     * 从 spring security context holder 中拿
     *
     * @return 操作日志中操作者信息默认值
     */
    @Override
    protected Operator resolveOperator(HttpServletRequest request) {
        ShoulderCurrentUserOperator operator = new ShoulderCurrentUserOperator();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return SystemOperator.getInstance();
        }
        operator.setUserId(authentication.getName());
        operator.setUserRealName(authentication.getName());

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            operator.setUserId(userDetails.getUsername());
        }

        Object details = authentication.getDetails();
        if (details instanceof WebAuthenticationDetails) {
            operator.setTerminalType(TerminalType.BROWSER);
            WebAuthenticationDetails webAuthenticationDetails = ((WebAuthenticationDetails) details);
            operator.setTerminalId(webAuthenticationDetails.getSessionId());
            operator.setRemoteAddress(webAuthenticationDetails.getRemoteAddress());
            operator.setTerminalInfo("User-Agent");
        }
        operator.setTerminalInfo(String.valueOf(AppContext.getLocale()));
        return operator;
    }
}
