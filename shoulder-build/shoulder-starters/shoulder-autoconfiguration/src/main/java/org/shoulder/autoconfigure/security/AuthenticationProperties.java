package org.shoulder.autoconfigure.security;

import lombok.Data;
import org.shoulder.security.authentication.AuthenticationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 认证类型 IDE 自动提示
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = "shoulder.security.auth")
public class AuthenticationProperties {

    /**
     * 认证类型
     * NestedConfigurationProperty IDE 自动提示
     */
    @NestedConfigurationProperty
    private AuthenticationType type = AuthenticationType.SESSION;
}
