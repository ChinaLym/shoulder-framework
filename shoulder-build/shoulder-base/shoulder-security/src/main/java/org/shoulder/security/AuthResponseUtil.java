package org.shoulder.security;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.util.JsonUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 返回 rest 格式的响应
 * {"msg":"xxx"}
 *
 * @author lym
 */
public class AuthResponseUtil {

    private static final String SUCCESS_RESPONSE = JsonUtils.toJson(RestResult.success());

    public static void authFail(HttpServletResponse response, AuthenticationException exception, ErrorCode errorCode)
        throws IOException {

        RestResult restResult = errorCode.toResponse(exception.getMessage());
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(JsonUtils.toJson(restResult));
    }

    public static void success(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(SUCCESS_RESPONSE);
    }

    public static void success(HttpServletResponse response, Object data) throws IOException {
        String responseStr = JsonUtils.toJson(RestResult.success(data));
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(responseStr);
    }

}
