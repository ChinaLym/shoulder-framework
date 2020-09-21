package org.shoulder.web.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.ColorString;
import org.shoulder.core.util.ColorStringBuilder;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
import org.shoulder.http.util.HttpLogHelper;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 彩色形式记录接口出入参数
 * <p>
 * Spring 的 RequestMappingHandlerMapping RequestResponseBodyMethodProcessor 也会记录 debug 日志
 * 但其记录日志的目的是为了便于排查spring框架的错误，而不是方便使用者查看，shoulder 的 logback.xml 默认屏蔽他们的 debug 日志
 * shoulder 这里的记录 Logger 是取的对应 Controller 的 Logger，且信息更多，如请求方、请求方法、请求路径、请求头、请求参数
 *
 * @author lym
 * @see CommonsRequestLoggingFilter spring 中提供的日志过滤器
 * @see RequestResponseBodyMethodProcessor
 * @see RequestMappingHandlerMapping
 * @see RequestResponseBodyMethodProcessor
 */
@Aspect
public class RestControllerColorfulLogAspect extends BaseRestControllerLogAspect {

    private static final String SELF_CLASS_NAME = RestControllerColorfulLogAspect.class.getSimpleName();

    protected static final boolean LOG_TILL_RESPONSE_DEFAULT = true;

    /**
     * 等待响应返回后再进行统一记录（分开打印会导致请求日志和响应日志不在一起，推荐在开发阶段打印在一起方便查看）。
     * tip:若请求过慢可能导致日志迟迟不打印
     */
    private final boolean logTillResponse;

    /**
     * 换行符
     */
    private static final String NEW_LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * 记录请求消耗时间
     */
    private ThreadLocal<Long> requestTimeLocal = new ThreadLocal<>();
    /**
     * 代码位置
     */
    private ThreadLocal<String> codeLocationLocal = new ThreadLocal<>();

    public RestControllerColorfulLogAspect(boolean useControllerLogger, boolean logTillResponse) {
        super(useControllerLogger);
        this.logTillResponse = logTillResponse;
    }

    /**
     * 记录入参
     *
     * @param jp 日志记录切点
     */
    @Override
    public void before(JoinPoint jp, Logger log) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        if (!log.isDebugEnabled()) {
            return;
        }

        String codeLocation = HttpLogHelper.genCodeLocationLink(method);
        codeLocationLocal.set(codeLocation);
        // 记录请求方法、路径，Controller 信息与代码位置
        HttpServletRequest request = ServletUtil.getRequest();
        ColorStringBuilder requestInfo = new ColorStringBuilder()
            .newLine()
            .cyan("//========================================== ")
            .yellow("Shoulder API Report", ColorString.Style.BOLD, true)
            .cyan(" (" + SELF_CLASS_NAME + ")")
            .cyan(" ==========================================\\\\")
            .newLine();

        // 请求地址
        requestInfo
            .green("Request   : ", ColorString.Style.BOLD)
            .append("[")
            .lBlue(request.getMethod().toUpperCase())
            .append("] ")
            .lBlue(request.getRequestURL().toString())
            .newLine();
        // 处理的 Controller
        requestInfo
            .green("Controller: ", ColorString.Style.BOLD)
            .append(codeLocation)
            .newLine();

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = jp.getArgs();

        requestInfo
            .green("From      : ", ColorString.Style.BOLD)
            .append(request.getRemoteAddr())
            .newLine();

        requestInfo
            .green("Headers   :", ColorString.Style.BOLD);

        Map<String, String> headers = ServletUtil.getRequestHeaders();
        headers.forEach((headerName, headerValue) -> requestInfo
            .newLine().tab()
            .lGreen(headerName)
            .tab()
            .blue(": ")
            .cyan(headerValue));

        // 记录 Controller 入参
        if (parameters.length > 0) {
            requestInfo.newLine()
                .green("Params    :", ColorString.Style.BOLD);
        }

        for (int i = 0; i < parameters.length; i++) {
            Class<?> argType = parameters[i].getType();
            String argName = parameterNames[i];
            String argValue = JsonUtils.toJson(args[i]);

            requestInfo
                .newLine().tab()
                .lBlue(argType.getSimpleName())
                .tab()
                .append(" ")
                .cyan(argName)
                .tab()
                .blue(": ")
                .lMagenta(argValue);
        }

        requestInfo.newLine();

        log.debug(requestInfo.toString());

        requestTimeLocal.set(System.currentTimeMillis());
    }


    @Override
    public void after(ProceedingJoinPoint jp, Logger log, Object returnObject) {
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        HttpServletResponse response = ServletUtil.getResponse();
        ColorStringBuilder responseInfo = new ColorStringBuilder().newLine();
        responseInfo
            .magenta("Controller : ", ColorString.Style.BOLD)
            .append(codeLocationLocal.get())
            .newLine();

        responseInfo
            .magenta("Cost:      : ", ColorString.Style.BOLD)
            .append(HttpLogHelper.cost(cost))
            .newLine();

        String statusStr = String.valueOf(response.getStatus());
        responseInfo
            .magenta("Status     : ", ColorString.Style.BOLD)
            .color(statusStr, HttpLogHelper.httpStatusColor(statusStr))
            .newLine();

        responseInfo
            .magenta("Headers    : ", ColorString.Style.BOLD);

        ServletUtil.getResponseHeaders()
            .forEach((headerName, headerValue) -> responseInfo
                .newLine().tab()
                .lMagenta(headerName)
                .tab()
                .green(": ")
                .cyan(headerValue));

        responseInfo.newLine();

        if (returnObject instanceof ModelAndView) {
            // 打印 model
            ModelAndView modelAndView = (ModelAndView) returnObject;
            Map<String, Object> model = modelAndView.getModel();
            responseInfo
                .magenta("Model      : ", ColorString.Style.BOLD);

            model.forEach((k, v) -> responseInfo
                .newLine().tab()
                .lMagenta(k)
                .tab()
                .green(": ")
                .cyan(v instanceof CharSequence ? String.valueOf(v) : JsonUtils.toJson(v)));
        } else {
            // 打印返回值
            String responseStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
            responseInfo
                .magenta("Result     : ", ColorString.Style.BOLD)
                .append(responseStr);
        }

        responseInfo.newLine()
            .cyan("\\\\========================== ")
            .lBlue(codeLocationLocal.get())
            .cyan(" ==========================//");

        log.debug(responseInfo.toString());
        cleanLocal();
    }


    /**
     * 清理线程变量
     */
    private void cleanLocal() {
        requestTimeLocal.remove();
        codeLocationLocal.remove();
    }

}
