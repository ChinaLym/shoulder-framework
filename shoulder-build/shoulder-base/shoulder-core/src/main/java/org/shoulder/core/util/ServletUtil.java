package org.shoulder.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public static boolean canGetRequest() {
        return RequestContextHolder.getRequestAttributes() != null;
    }

    @Nonnull
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

    private static final String IP_UTILS_FLAG = ",";
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";

    /**
     * 获取IP地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getRemoteAddress() {
        return getRemoteAddress(getRequest());
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        String ip = null;
        try {
            //以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
            ip = request.getHeader("X-Original-Forwarded-For");
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            //获取nginx等代理的ip
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("x-forwarded-for");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            //兼容k8s集群获取ip
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                    //根据网卡取本机配置的IP
                    InetAddress iNet = null;
                    try {
                        iNet = InetAddress.getLocalHost();
                        ip = iNet.getHostAddress();
                    } catch (UnknownHostException e) {
                        logger.error("getClientIp error: {}", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("IPUtils ERROR ", e);
        }
        //使用代理，则获取第一个IP地址
        if (!StringUtils.isEmpty(ip) && ip.indexOf(IP_UTILS_FLAG) > 0) {
            ip = ip.substring(0, ip.indexOf(IP_UTILS_FLAG));
        }

        return ip;
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
        return XML_HTTP_REQ_VALUE.equalsIgnoreCase(StringUtils.trim(requestHeader));
    }

    /**
     * 判断请求是否为浏览器发送的请求
     *
     * @param request 请求
     * @return 判断结果
     */
    public static boolean isBrowser(final HttpServletRequest request) {
        return StringUtils.startsWith(request.getHeader("User-Agent"), "Mozilla");
    }

    /**
     * 判断请求是否可接受页面/跳转类响应
     *
     * @param request 请求
     * @return 判断结果
     */
    public static boolean isAcceptPage(final HttpServletRequest request) {
        return request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_HTML_VALUE)
            || request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.ALL_VALUE);
    }

    /**
     * 判断请求是否为浏览器发送的页面请求
     *
     * @param request 请求
     * @return 判断结果
     */
    public static boolean isBrowserPage(final HttpServletRequest request) {
        // 如果 User-Agent 为 Mozilla 开头，认为是浏览器请求
        return isBrowser(request)
            // 可接受页面响应
            && isAcceptPage(request)
            // 不是 ajax
            && !isAjax(request);


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
