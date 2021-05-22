package org.shoulder.security.authentication.handler.json;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * 从请求中获取 clientId、clientSecret
 *
 * @author lym
 */
public class FormTokenAuthenticationSuccessHandler extends BasicAuthorizationTokenAuthenticationSuccessHandler {

    private final String usernameParamName;

    private final String passwordParamName;

    public FormTokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService, AuthorizationServerTokenServices authorizationServerTokenServices) {
        this("username", "password", clientDetailsService, authorizationServerTokenServices);
    }

    public FormTokenAuthenticationSuccessHandler(String usernameParamName, String passwordParamName, ClientDetailsService clientDetailsService, AuthorizationServerTokenServices authorizationServerTokenServices) {
        super(clientDetailsService, authorizationServerTokenServices);
        AssertUtils.notBlank(usernameParamName, CommonErrorCodeEnum.CODING);
        AssertUtils.notBlank(passwordParamName, CommonErrorCodeEnum.CODING);
        this.usernameParamName = usernameParamName;
        this.passwordParamName = passwordParamName;
    }

    @Override
    protected String[] extractClientInfo(@Nonnull HttpServletRequest request) throws UnapprovedClientAuthenticationException {
        // todo 参数
        AssertUtils.notNull(request.getParameterValues(usernameParamName), CommonErrorCodeEnum.PARAM_BODY_NOT_READABLE);
        AssertUtils.notNull(request.getParameterValues(passwordParamName), CommonErrorCodeEnum.PARAM_BODY_NOT_READABLE);

        return new String[]{request.getParameterValues(usernameParamName)[0], request.getParameterValues(passwordParamName)[0]};
    }

}