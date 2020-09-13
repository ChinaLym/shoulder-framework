package org.shoulder.code.processor;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.exception.ValidateCodeAuthenticationException;
import org.shoulder.code.exception.ValidateCodeException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

/**
 * 验证码处理器
 *
 * @author lym
 */
public interface ValidateCodeProcessor extends ValidateCodeType {

    /**
     * 创建验证码
     *
     * @param request 待创建请求
     * @throws ValidateCodeException 生成验证码失败
     */
    void create(ServletWebRequest request) throws ValidateCodeException;

    /**
     * 校验验证码
     *
     * @param servletWebRequest 待校验请求
     * @throws ValidateCodeAuthenticationException 校验不通过
     */
    void validate(ServletWebRequest servletWebRequest) throws ValidateCodeAuthenticationException;


    /**
     * 负责处理的 url，推荐通过配置文件获取
     */
    List<String> processedUrls();
}
