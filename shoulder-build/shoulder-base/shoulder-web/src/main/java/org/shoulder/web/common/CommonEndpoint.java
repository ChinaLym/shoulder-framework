package org.shoulder.web.common;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * WEB 应用通用的接口
 *
 * @author lym
 */
@Tag(name = "通用接口-CommonEndpoint")
@Order
@RestController
public class CommonEndpoint implements ApplicationListener<ApplicationStartedEvent> {

    private volatile boolean hasStart = false;

    @RequestMapping({"/redirect/**"})
    public String redirect(HttpServletRequest request, HttpServletResponse response,
                           @RequestHeader(value = "Host") String host) throws IOException {
        String requestUri = request.getRequestURI();
        String ipPort = request.getScheme() + "://" + host;
        StringBuilder redirectAddress = new StringBuilder(ipPort);
        redirectAddress.append(requestUri.substring(requestUri.indexOf("redirect") + 8));
        if (StringUtils.isNotEmpty(request.getQueryString())) {
            redirectAddress.append("?").append(request.getQueryString());
        }
        String redirect = redirectAddress.toString();
        response.sendRedirect(redirect);
        return redirect;
    }

    @RequestMapping({"/current/user"})
    public String currentUser() {
        // todo P2 getUserId
        return AppContext.getUserId();
    }

    @RequestMapping({"/health/check"})
    public int healthCheck() {
        return hasStart ? 0 : 1;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        hasStart = true;
    }
}
