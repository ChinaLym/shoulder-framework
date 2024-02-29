package org.shoulder.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * <p>
 * 这是一个不需要 EagleEye 依赖的特殊版本，可以在没有 EagleEye 的环境运行。
 * 如果初始化时从当前 ClassLoader 没有办法加载到 EagleEye 类，就不做埋点。
 */
public class EagleEyeFilter2 implements Filter {

	private static final String USE_LOCAL_IP = "useLocalIp";

	private boolean useLocalIp = false;

	private boolean traceEnabled = false;
	private Method getTraceId = null;
	private Method startTrace = null;
	private Method endTrace = null;
	private Method getRemoteAddress = null;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
		if (!tracable(request, response)) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			String ip = null;
			if (!useLocalIp) {
				ip = (String) getRemoteAddress.invoke(null, httpRequest);
			}
			String traceId = (String) getTraceId.invoke(null, httpRequest, ip);
			startTrace.invoke(null, traceId, httpRequest, httpResponse);
		} catch (Throwable t) {
			traceEnabled = false;
			System.err.println("EagleEye trace is disabled due to startTrace error: " + t.getMessage());
			// ignore
		}

		try {
			chain.doFilter(request, response);
		} finally {
			try {
				endTrace.invoke(null, httpRequest, httpResponse);
			} catch (Throwable t) {
				traceEnabled = false;
				System.err.println("EagleEye trace is disabled due to endTrace error: " + t.getMessage());
				// ignore
			}
		}
	}

	private boolean tracable(final ServletRequest request, final ServletResponse response) {
		return traceEnabled && request instanceof HttpServletRequest && response instanceof HttpServletResponse;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String uselocal = filterConfig.getInitParameter(USE_LOCAL_IP);
		if(uselocal != null && "true".equals(uselocal)){
			useLocalIp = true;
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> clazz = Class.forName("com.taobao.eagleeye.EagleEyeRequestTracer", true, classLoader);
			Method getTraceId = clazz.getDeclaredMethod("getTraceId", HttpServletRequest.class, String.class);
			getTraceId.setAccessible(true);
			Method startTrace = clazz.getDeclaredMethod("startTrace", String.class, HttpServletRequest.class, HttpServletResponse.class);
			startTrace.setAccessible(true);
			Method endTrace = clazz.getDeclaredMethod("endTrace", HttpServletRequest.class, HttpServletResponse.class);
			endTrace.setAccessible(true);
			Method getRemoteAddress = clazz.getDeclaredMethod("getRemoteAddress", HttpServletRequest.class);
			getRemoteAddress.setAccessible(true);

			Class<?> clazz2 = Class.forName("com.taobao.eagleeye.EagleEye", true, classLoader);
			Method selfLog = clazz2.getDeclaredMethod("selfLog", String.class);
			selfLog.setAccessible(true);
			selfLog.invoke(null, "[INFO] " + this.getClass().getSimpleName() +
					" initialized successfully, useLocalIp=" + useLocalIp);

			this.getTraceId = getTraceId;
			this.startTrace = startTrace;
			this.endTrace = endTrace;
			this.getRemoteAddress = getRemoteAddress;
			traceEnabled = true;
		} catch (Throwable t) {
			traceEnabled = false;
			System.err.println("EagleEye trace is disabled due to initialization error: " + t.getMessage());
		}
	}

	@Override
	public void destroy() {
	}
}
