package org.shoulder.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.log.beautify.ColorString;
import org.shoulder.core.log.beautify.ColorStringBuilder;
import org.shoulder.core.log.beautify.LogHelper;
import org.shoulder.core.util.FileUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
import org.shoulder.http.util.HttpLogHelper;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.Serializable;
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

        String codeLocation = LogHelper.genCodeLocationLink(method);
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
                // 0:0:0:0:0:0:0:1 127.0.0.1 本机
                .append(request.getRemoteAddr())
                .newLine();

        requestInfo
                .green("Headers   :", ColorString.Style.BOLD);

        HttpLogHelper.appendHeader(requestInfo, ServletUtil.getRequestHeaders());

        // 记录 Controller 入参
        if (parameters.length > 0) {
            requestInfo.newLine()
                    .green("Params    :", ColorString.Style.BOLD);
        }

        for (int i = 0; i < parameters.length; i++) {
            Class<?> argType = parameters[i].getType();
            String argValue = toLogValue(args[i]);
            String argName = parameterNames[i];
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
    }

    static String toLogValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof MultipartFile) {
            return new MultiFileInfo((MultipartFile) value).toString();
        }
        if (!(value instanceof Serializable)) {
            // 流类型，或者带有流属性的DTO跳过
            // ServletResponse || value instanceof ServletRequest || value instanceof Closeable || value instanceof InputStreamSource
            return "SKIP_LOG:NOT_Serializable";
        }
        try {
            return JsonUtils.toJson(value);
        } catch (Exception e) {
            ShoulderLoggers.SHOULDER_CONFIG.warnWithErrorCode(CommonErrorCodeEnum.UNKNOWN.getCode(), "This param type={} not support json, skip", value.getClass().getName());
            return "SKIP_LOG:NOT_SUPPORT_JSON";
        }
    }


    @Override
    public void after(ProceedingJoinPoint jp, Logger log, Object returnObject, long cost) {
        String codeLine = codeLocationLocal.get();
        codeLocationLocal.remove();

        HttpServletResponse response = ServletUtil.getResponse();
        ColorStringBuilder responseInfo = new ColorStringBuilder().newLine();
        responseInfo
                .magenta("Controller : ", ColorString.Style.BOLD)
                .append(codeLine)
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

        if (returnObject instanceof ModelAndView modelAndView) {
            // 打印 model
            Map<String, Object> model = modelAndView.getModel();
            responseInfo
                    .magenta("Model      : ", ColorString.Style.BOLD);

            model.forEach((k, v) -> responseInfo
                    .newLine().tab()
                    .lMagenta(k)
                    .tab()
                    .green(": ")
                    .cyan(v instanceof CharSequence ? String.valueOf(v) : toLogValue(v)));
        } else {
            // 打印返回值
            String responseStr = returnObject != null ? toLogValue(returnObject) : "null";
            responseInfo
                    .magenta("Result     : ", ColorString.Style.BOLD)
                    .append(responseStr);
        }

        responseInfo.newLine()
                .cyan("\\\\========================== ")
                .lBlue(codeLine)
                .cyan(" ==========================//");

        log.debug(responseInfo.toString());
    }

    public static class MultiFileInfo {

        private String name;
        private String originalFilename;
        private String contentType;
        private long size;
        private boolean empty;

        public MultiFileInfo() {
        }

        public MultiFileInfo(MultipartFile multipartFile) {
            this.name = multipartFile.getName();
            this.originalFilename = multipartFile.getOriginalFilename();
            this.contentType = multipartFile.getContentType();
            this.size = multipartFile.getSize();
            this.empty = multipartFile.isEmpty();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public void setOriginalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", originalFilename='" + originalFilename + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", size=" + size + "byte (" + FileUtils.byteCountToDisplay(size) +
                    "), empty=" + empty +
                    '}';
        }

    }

}
