package org.shoulder.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 请求相关工具类，需要在
 *
 * @author lym
 */
public class ServletUtil {

    private static final String XML_HTTP_REQ_VALUE = "XMLHttpRequest";

    private static Logger logger = LoggerFactory.getLogger(ServletUtil.class);

    public static String getCookie(String cookieName) {
        HttpServletRequest request = ServletUtil.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void setCookie(String cookieName, String cookieValue, int maxSecond) {
        HttpServletResponse response = ServletUtil.getResponse();
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxSecond);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void expireCookie(String cookieName) {
        HttpServletResponse response = ServletUtil.getResponse();
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    public static HttpSession getSession() {
        return getRequestAttributes().getRequest().getSession();
    }

    public static Object getSessionAttribute(String sessionKeyName) {
        return getSession().getAttribute(sessionKeyName);
    }

    public static void setSessionAttribute(String sessionKey, Object value) {
        getSession().setAttribute(sessionKey, value);
    }

    @NonNull
    public static ServletRequestAttributes getRequestAttributes() {
        ServletRequestAttributes sa = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (sa == null) {
            throw new IllegalStateException("Not a servlet context!");
        }
        return sa;
    }

    public static Map<String, String> getRequestHeaders() {
        return getRequestHeaders(getRequest(), false);
    }

    public static Map<String, String> getRequestHeaders(HttpServletRequest request, boolean sortByKey) {
        Map<String, String> headers = sortByKey ? new TreeMap<>() : new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        // header 按照字母排序，方便查看
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    public static Map<String, String> getResponseHeaders() {
        return getResponseHeaders(getResponse(), false);
    }

    public static Map<String, String> getResponseHeaders(HttpServletResponse response, boolean sortByKey) {
        Collection<String> headerNames = response.getHeaderNames();
        Map<String, String> headers = sortByKey ? new TreeMap<>() : new HashMap<>(headerNames.size());
        // header 按照字母排序，方便查看
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    /**
     * 判断请求是否为ajax请求
     *
     * @param request 请求
     * @return 判断结果
     */
    public static boolean isAjax(final HttpServletRequest request) {
        // 如果 requestType 值为 XMLHttpRequest ，证明请求为ajax请求
        String requestHeader = request.getHeader("X-Requested-With");
        return XML_HTTP_REQ_VALUE.equalsIgnoreCase(requestHeader.trim());
    }

    /**
     * 需要返回application/json格式
     *
     * @param request 请求
     * @return 判断结果
     */
    public static boolean acceptJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept.startsWith("application/json");
    }

    public static boolean isAjax() {
        return isAjax(getRequest());
    }

    /**
     * 是否需要返回application/json格式
     */
    public static boolean needReturnJson() {
        HttpServletRequest request = getRequest();
        return acceptJson(request) || isAjax(request);
    }


    /**
     * 获取访问本应用/服务器的路径，若过代理，则可能会不正确！
     *
     * @return http://xxx.com:8080 拼接路径时最后需要添加 '/'
     */
    public static String getServerURL() {
        HttpServletRequest request = getRequest();
        String requestUrl = request.getRequestURL().toString();
        StringBuilder serverUrl = new StringBuilder("http");
        if (requestUrl.indexOf("https") > 0) {
            serverUrl.append("s://");
        } else {
            serverUrl.append("://");
        }
        serverUrl.append(request.getServerName());
        serverUrl.append(':');
        serverUrl.append(request.getServerPort());
        return serverUrl.toString();
    }

}
