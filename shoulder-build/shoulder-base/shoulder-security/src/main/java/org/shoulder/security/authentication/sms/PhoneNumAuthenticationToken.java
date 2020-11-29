package org.shoulder.security.authentication.sms;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

/**
 * 手机号码认证 token，用于保存请求中的手机号。
 * 实现了 AbstractAuthenticationToken 相当于一种策略类
 * 参考 {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 *
 * @author lym
 */
public class PhoneNumAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    // 实例属性
    // ================================================================================================

    /**
     * 认证凭证标识，这里是 UserDetails，为空时说明只是创建了 token、未认证
     */
    private final Object principal;

    // 构造器
    // ===================================================================================================

    /**
     * 构建一个未授权的 SmsCodeAuthenticationToken
     */
    public PhoneNumAuthenticationToken(String phoneNumber) {
        super(null);
        this.principal = phoneNumber;
        setAuthenticated(false);
    }

    /**
     * 创建一个认证过的 token。该方法只应被
     * {@link AuthenticationManager} 或 {@link AuthenticationProvider} 的实现类调用
     *
     * @param principal   用户信息 UserDetails
     * @param authorities 授权信息
     */
    public PhoneNumAuthenticationToken(Object principal,
                                       Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true); // must use super, as we override
    }

    // ========================================================================================================

    /**
     * sms 无证明信息
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * 标志凭证
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

}
