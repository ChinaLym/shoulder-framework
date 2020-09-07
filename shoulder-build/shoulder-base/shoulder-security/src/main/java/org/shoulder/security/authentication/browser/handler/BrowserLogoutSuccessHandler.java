package org.shoulder.security.authentication.browser.handler;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.util.JsonUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 40
 *
 * @author lym
 */
public class BrowserLogoutSuccessHandler implements LogoutSuccessHandler {

    public BrowserLogoutSuccessHandler(String signOutSuccessUrl) {
        this.signOutSuccessUrl = signOutSuccessUrl;
    }

    private String signOutSuccessUrl;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        if (StringUtils.isBlank(signOutSuccessUrl)) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(JsonUtils.toJson(BaseResponse.success()));
        } else {
            response.sendRedirect(signOutSuccessUrl);
        }

    }

}
