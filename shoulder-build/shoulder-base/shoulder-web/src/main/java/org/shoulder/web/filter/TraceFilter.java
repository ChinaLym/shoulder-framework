package org.shoulder.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.AddressUtils;
import org.shoulder.core.util.ServletUtil;
import org.shoulder.core.util.StringUtils;

import java.io.IOException;

/**
 * 链路追踪过滤器，确保上下文中有trace
 * 使用者可以自行实现替代
 *
 * @author lym
 */
//可见字符总长度 = 2 + 2 + 4 + 13 + 4 + 1 + 32 = 58 字符
public class TraceFilter implements Filter {

    private final String TRACE_ID_IN_HEADER = "X-TraceId";
    private final String TRACE_ID_IN_PARAM = "traceId";

    private static final String USE_LOCAL_IP = "useLocalIp";

    private boolean useLocalIp = false;

    private boolean traceEnabled = false;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!isEnable(request, response)) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 执行前确保有 traceId、spanId

        try {
            String traceId = getTraceIdOrGenerate(httpRequest);
            startTrace(traceId, httpRequest, httpResponse);
        } catch (Exception e) {
            System.err.println("trace Start Fail");
            e.printStackTrace();
        }

        try {
            filterChain.doFilter(httpRequest, httpResponse);
        } finally {
            try {
                endTrace(httpRequest, httpResponse);
            } catch (Exception e) {
                System.err.println("trace End Fail");
                e.printStackTrace();
            }
        }
    }

    private void startTrace(String traceId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AppContext.setTraceId(traceId);
        httpResponse.setHeader(TRACE_ID_IN_HEADER, traceId);
        // TODO SPAN ID 透传的数据、用户信息等
    }

    private void endTrace(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // 计算trace耗时、发送trace等
        // 解析状态码，成功失败等
    }

    private String getTraceIdOrGenerate(HttpServletRequest request) {
        // 幂等
        String localTraceId = AppContext.getTraceId();
        if (StringUtils.isNotBlank(localTraceId)) {
            return localTraceId;
        }
        try {
            // todo validate replace notBlank
            //1. parse From RequestQueryString
            String paramTrace = StringUtils.trim(request.getParameter(TRACE_ID_IN_PARAM));
            if (StringUtils.isNotBlank(paramTrace)) {
                return paramTrace;
            }

            //2. parse From Request Header
            String headerTrace = StringUtils.trim(request.getHeader(TRACE_ID_IN_HEADER));
            if (StringUtils.isNotBlank(headerTrace)) {
                return headerTrace;
            }
        } catch (Exception ignore) {
        }

        // generate
        String ip = useLocalIp ? AddressUtils.getIp() : ServletUtil.getRemoteAddress(request);
        return TraceIdGenerator.generateTraceIdWithIpV4(ip);

    }

    private boolean isEnable(final ServletRequest request, final ServletResponse response) {
        return traceEnabled && request instanceof HttpServletRequest && response instanceof HttpServletResponse;
    }

    private String resolveTraceId(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader(TRACE_ID_IN_HEADER);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String useLocalAsIp = filterConfig.getInitParameter(USE_LOCAL_IP);
        if ("true".equalsIgnoreCase(useLocalAsIp)) {
            useLocalIp = true;
        }
        // log ip
    }
}
