package org.shoulder.web.common;

import org.shoulder.core.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * WEB 应用通用的接口
 *
 * @author lym
 */
@RestController
public class CommonEndpoint {

    @RequestMapping({"/redirect/**"})
    public String redirect(HttpServletRequest request, HttpServletResponse response,
                           @RequestHeader(value = "Host") String host) throws IOException {
        String requestURI = request.getRequestURI();
        String ipPort = request.getScheme() + "://" + host;
        StringBuilder redirectAddress = new StringBuilder(ipPort);
        redirectAddress.append(requestURI.substring(requestURI.indexOf("redirect") + 8));
        if (StringUtils.isNotEmpty(request.getQueryString())) {
            redirectAddress.append("?").append(request.getQueryString());
        }
        String redirect = redirectAddress.toString();
        response.sendRedirect(redirect);
        return redirect;
    }

    @RequestMapping({"/current/user"})
    public String currentUser() throws IOException {
        return null;
    }


}
