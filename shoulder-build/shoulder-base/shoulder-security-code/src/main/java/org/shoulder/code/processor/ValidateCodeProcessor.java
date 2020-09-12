package org.shoulder.code.processor;

import org.shoulder.code.ValidateCodeType;
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
     * 创建
     */
    void create(ServletWebRequest request) throws ValidateCodeException;

    /**
     * 校验
     */
    void validate(ServletWebRequest servletWebRequest);


    /**
     * 负责处理的 url，推荐通过配置文件获取
     */
    List<String> processedUrls();
}
