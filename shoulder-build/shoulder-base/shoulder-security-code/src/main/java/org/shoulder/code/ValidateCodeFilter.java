package org.shoulder.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 验证码过滤器
 *
 * @author lym
 */
public class ValidateCodeFilter extends OncePerRequestFilter {

    /**
     * 验证码校验失败处理器
     */
    private AuthenticationFailureHandler authenticationFailureHandler;

    /**
     * 系统中的校验码处理器
     */
    private ValidateCodeProcessorHolder validateCodeProcessorHolder;

    /**
     * 存放所有需要校验验证码的 url 和 对应的类型
     */
    private Set<ValidateCodeRule> needValidateUrlSet = new HashSet<>();
    /**
     * 验证请求url与配置的url是否匹配的工具类
     */
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    public ValidateCodeFilter(AuthenticationFailureHandler authenticationFailureHandler, ValidateCodeProcessorHolder validateCodeProcessorHolder) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.validateCodeProcessorHolder = validateCodeProcessorHolder;
    }

    /**
     * 初始化要拦截的url配置信息（提交登录信息的url）
     */
    @Override
    public void initFilterBean() throws ServletException {
        super.afterPropertiesSet();

        List<ValidateCodeProcessor> allProcessors = validateCodeProcessorHolder.getAllProcessors();
        for (ValidateCodeProcessor allProcessor : allProcessors) {
            addValidateRule(allProcessor.processedUrls(), allProcessor.getType());
        }
    }

    /**
     * 系统中配置的需要校验验证码的URL根据校验的类型放入map
     */
    protected void addValidateRule(List<String> urls, String type) {
        if (CollectionUtils.isEmpty(urls) || StringUtils.isBlank(type)) {
            return;
        }
        urls.forEach(url -> addValidateRule(url, type));
    }

    /**
     * 系统中配置的需要校验验证码的URL根据校验的类型放入map
     */
    protected void addValidateRule(String url, String type) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(type)) {
            return;
        }
        needValidateUrlSet.add(new ValidateCodeRule(url, type));
    }


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain chain)
        throws ServletException, IOException {

        String type;
        boolean debug = logger.isDebugEnabled();

        // 获取是否需要校验，以及对应的处理器类型
        if ((type = getCodeType(request)) != null) {

            if (debug) {
                logger.debug("ValidateCodeFilter filter(" + request.getRequestURI() + "), code type is " + type);
            }
            try {
                validateCodeProcessorHolder.getProcessor(type)
                    .validate(new ServletWebRequest(request, response));
                if (debug) {
                    logger.debug("validateCode success " + request.getRequestURI());
                }
            } catch (Exception e) {
                if (authenticationFailureHandler != null) {
                    authenticationFailureHandler.onAuthenticationFailure(request, response,
                        new AuthenticationException(e.getMessage() + type, e) {
                        });
                }
                throw e;
            }
        }

        // 执行请求
        chain.doFilter(request, response);

    }

    /**
     * 判断请求是否需要拦截，并校验验证码
     *
     * @param request 请求
     * @return needValidate ? validateCodeType : null
     */
    private String getCodeType(HttpServletRequest request) {
        // 登录请求都是 POST, 如果是 GET 则直接放行
        String currentRequestUri = request.getRequestURI();
        for (ValidateCodeRule rule : needValidateUrlSet) {
            if (pathMatcher.match(rule.getUrlPattern(), currentRequestUri)) {
                return rule.getType();
            }
        }

        return null;
    }

    /**
     * 验证码路径校验规则
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateCodeRule {
        /**
         * 支持 ant 匹配
         */
        String urlPattern;

        /**
         * 需要校验的格式
         */
        String type;

    }

}
