package org.shoulder.code;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.shoulder.security.SecurityConst;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 验证码过滤器
 *
 * @author lym
 */
public class ValidateCodeFilter extends OncePerRequestFilter implements InitializingBean {

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
    private Map<String, String> needValidateUrlAndTypeMap = new HashMap<>();
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
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        // 两个默认的处理：表单登录默认需要验证码，手机短信验证码登录默认需要校验短信验证码
        addUrlToMap(SecurityConst.URL_AUTHENTICATION_FORM, ValidateCodeConsts.IMAGE);
        addUrlToMap(SecurityConst.URL_AUTHENTICATION_SMS, ValidateCodeConsts.SMS);

        List<ValidateCodeProcessor> allProcessors = validateCodeProcessorHolder.getAllProcessors();
        for (int i = 0; i < allProcessors.size(); i++) {
            addUrlToMap(allProcessors.get(i).processedUrls(), allProcessors.get(i).getType());
        }
    }

    /**
     * 系统中配置的需要校验验证码的URL根据校验的类型放入map
     */
    protected void addUrlToMap(List<String> urls, String type) {
        if (CollectionUtils.isEmpty(urls) || StringUtils.isBlank(type))
            return;
        urls.forEach(url -> addUrlToMap(url, type));
    }

    /**
     * 系统中配置的需要校验验证码的URL根据校验的类型放入map
     */
    protected void addUrlToMap(String url, String type) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(type))
            return;
        needValidateUrlAndTypeMap.put(url, type);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
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
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            Set<String> urls = needValidateUrlAndTypeMap.keySet();
            for (String url : urls) {
                if (pathMatcher.match(url, request.getRequestURI())) {
                    return needValidateUrlAndTypeMap.get(url);
                }
            }
        }

        return null;
    }

}
