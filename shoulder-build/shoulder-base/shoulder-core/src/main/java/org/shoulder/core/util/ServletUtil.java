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

/**
* 请求相关工具类，需要在
 *
* @author lym
 */
public class ServletUtil {

	private static Logger logger = LoggerFactory.getLogger(ServletUtil.class);

	public static String getCookie(String cookieName){
		HttpServletRequest request = ServletUtil.getRequest();
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static void setCookie(String cookieName, String cookieValue, int maxSecond){
		HttpServletResponse response = ServletUtil.getResponse();
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(maxSecond);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public static void expireCookie(String cookieName){
		HttpServletResponse response = ServletUtil.getResponse();
		Cookie cookie = new Cookie(cookieName, "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public static HttpServletRequest getRequest(){
		return getRequestAttributes().getRequest();
	}

	public static HttpServletResponse getResponse(){
		return getRequestAttributes().getResponse();
	}

	public static HttpSession getSession(){
		return getRequestAttributes().getRequest().getSession();
	}

	public static Object getSessionAttribute(String sessionKeyName){
		return getSession().getAttribute(sessionKeyName);
	}

	public static void setSessionAttribute(String sessionKey, Object value){
		getSession().setAttribute(sessionKey, value);
	}

	@NonNull
	public static ServletRequestAttributes getRequestAttributes(){
		ServletRequestAttributes sa = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
		if(sa == null){
			throw new IllegalStateException("Not a servlet context!");
		}
		return sa;
	}

	private static final String XML_HTTP_REQ_VALUE = "XMLHttpRequest";

	/**
	 * 判断请求是否为ajax请求
	 *
	 * @param request 请求
	 * @return boolean型判断结果
	 */
	public static boolean isAjax(final HttpServletRequest request) {
		// 如果 requestType 值为 XMLHttpRequest ，证明请求为ajax请求
		String requestHeader = request.getHeader("X-Requested-With");
		return XML_HTTP_REQ_VALUE.equalsIgnoreCase(requestHeader.trim());
	}

	public static boolean isAjax() {
		return isAjax(getRequest());
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
