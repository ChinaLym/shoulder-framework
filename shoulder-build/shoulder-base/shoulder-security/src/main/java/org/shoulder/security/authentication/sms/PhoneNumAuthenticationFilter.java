package org.shoulder.security.authentication.sms;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.security.SecurityConst;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

/**
 * 支持手机号码认证方式，用于拦截获取手机号，生成 {@link PhoneNumAuthenticationToken}，不关心如何认证
 * 参考了 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
 * spring security 约定：需要确保该类（认证通过滤器类）加在验证码过滤器（认证校验过滤器）后，被其保护，否则会造成纯手机号无条件登录问题！！！
 *
 * 使用方：框架使用者，弱感知
 * 类定位：添加 sms 认证接口路径，使得
 *
 * @author lym
 */
public class PhoneNumAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // ~ Static fields/initializers
    // =====================================================================================

    /**
     * 手机号登录时，手机号参数名
     */
    private String phoneNumberParameter = SecurityConst.AUTHENTICATION_SMS_PARAMETER_NAME;
    /**
     * 是否只支持 post 请求
     */
    private boolean postOnly = true;

    // ~ Constructors
    // ===================================================================================================

    public PhoneNumAuthenticationFilter() {
        super(new AntPathRequestMatcher(SecurityConst.URL_AUTHENTICATION_SMS, "POST"));
    }

    // ~ Methods
    // ========================================================================================================

    /**
     * 从请求中获取手机号，生成
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        // 仅支持 POST 请求
        if (postOnly && !"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String phoneNumber = obtainPhoneNumber(request);

        // 由于 obtainPhoneNumber 可扩展，因此判断需要加在这里
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        // 去掉空格
        phoneNumber = phoneNumber.trim();

        PhoneNumAuthenticationToken authRequest = new PhoneNumAuthenticationToken(phoneNumber);

        // 允许子类通过 setDetails 设置一些额外的属性
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }


    /**
     * 这里将获取手机号，提取为 protected 方法，以便于子类扩展
     */
    protected String obtainPhoneNumber(HttpServletRequest request) {
        return request.getParameter(phoneNumberParameter);
    }

    /**
     * Provided so that subclasses may configure what is put into the
     * authentication request's details property.
     *
     * @param request     that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details set
     */
    protected void setDetails(HttpServletRequest request, PhoneNumAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    /**
     * Defines whether only HTTP POST requests will be allowed by this filter.
     * If set to true, and an authentication request is received which is not a
     * POST request, an exception will be raised immediately and authentication
     * will not be attempted. The <tt>unsuccessfulAuthentication()</tt> method
     * will be called as if handling a failed authentication.
     * <p>
     * Defaults to <tt>true</tt> but may be overridden by subclasses.
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getPhoneNumberParameter() {
        return phoneNumberParameter;
    }

    /**
     * Sets the parameter name which will be used to obtain the phoneNumber from
     * the login request.
     *
     * @param usernameParameter the parameter name. Defaults to "phoneNumber".
     */
    public void setPhoneNumberParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "phoneNumber parameter must not be empty or null");
        this.phoneNumberParameter = usernameParameter;
    }

}
