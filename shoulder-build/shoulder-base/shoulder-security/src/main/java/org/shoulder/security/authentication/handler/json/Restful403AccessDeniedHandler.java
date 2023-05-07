package org.shoulder.security.authentication.handler.json;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Token 模式 JSON 响应 认证过的用户访问无权限资源时的异常
 *
 * @author lym
 */
public class Restful403AccessDeniedHandler implements AccessDeniedHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("accessDenied 403 for: " + accessDeniedException.getMessage(), accessDeniedException);

        BaseResult<Void> result = BaseResult.error(CommonErrorCodeEnum.PERMISSION_DENY,
                accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(AppInfo.charset().toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String resBody = JsonUtils.toJson(result);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
    }

}
