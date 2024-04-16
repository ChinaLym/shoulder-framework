package org.shoulder.web.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * WEB 应用通用的接口
 *
 * @author lym
 * @see ApplicationAvailabilityAutoConfiguration
 */
@Tag(name = "通用接口-CommonEndpoint")
@Order
@RestController
public class CommonEndpoint extends ApplicationAvailabilityBean {

    @Operation(summary = "重定向", description = "重定向到xx地址")
    @RequestMapping(value = "/redirect/**", method = { RequestMethod.GET, RequestMethod.POST})
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

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current/user")
    public String currentUser() {
        return AppContext.getUserId();
    }

    @Operation(summary = "健康检查", description = "判断应用是否启动完成/是否夯住，可以对外提供服务。0: 启动成功 && 可接受流量；1: 启动中；2：启动了，但目前无法接受流量和请求；3：启动失败。")
    @GetMapping("/health/check")
    public int healthCheck() {
        LivenessState livenessState = getState(LivenessState.class);
        ReadinessState readinessState = getState(ReadinessState.class);
        if(livenessState == LivenessState.CORRECT) {
            if(readinessState == ReadinessState.ACCEPTING_TRAFFIC) {
                // 启动成功 && 可接受流量
                return 0;
            }else if (readinessState == ReadinessState.REFUSING_TRAFFIC) {
                // 启动了，但暂不能处理流量
                return 2;
            }
            // 启动中，状态不明
            return 1;
        } else if (livenessState == LivenessState.BROKEN) {
            // 启动失败
            return 3;
        } else {
            // 启动中，状态不明
            return 1;
        }
    }

}
