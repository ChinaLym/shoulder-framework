package org.shoulder.security.authentication.browser.handler;

import org.shoulder.security.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下认证失败的默认处理器
 * 继承了 spring 的默认处理器，在其基础上新增返回 json 类型
 * 实际应用中如果由更复杂的判断逻辑，可继承该类或实现个性的 AuthenticationFailureHandler 并注入
 *
 * @author lym
 */
public class BrowserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ResponseType responseType;

    public BrowserAuthenticationFailureHandler(ResponseType responseType) {
        this.responseType = responseType;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.info("authentication FAIL.");

        if (ResponseType.JSON.equals(responseType)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(ResponseUtil.jsonMsg(exception.getMessage()));
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }

    }
}
