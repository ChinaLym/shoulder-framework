package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

/**
 * 操作日志-当前用户信息
 *
 * @author lym
 */
@Configuration
@ConditionalOnClass(value = {OperationLogDTO.class, Authentication.class})
@AutoConfigureBefore(OperationLogWebAutoConfiguration.class)
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class SpringSecurityCurrentUserConfiguration {

    /**
     * web 上下文中对用户信息的 AOP 解析
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationLogOperatorInfoInterceptor springSecurityOperatorInfoInterceptor() {
        return new SpringSecurityOperatorInfoInterceptor();
    }
}
