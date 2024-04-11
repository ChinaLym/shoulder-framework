package org.shoulder.http.interceptor;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.log.beautify.ColorString;
import org.shoulder.core.log.beautify.ColorStringBuilder;
import org.shoulder.core.log.beautify.LogHelper;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.http.util.HttpLogHelper;
import org.springframework.web.client.RestTemplate;

/**
 * 为 RestTemplate 增加记录日志的能力【开发态】
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数、返回值
 *
 * @author lym
 */
public class RestTemplateColorfulLogInterceptor extends BaseRestTemplateLogInterceptor {

    private final Logger log = ShoulderLoggers.SHOULDER_CLIENT;

    private static final String SELF_CLASS_NAME = RestTemplateColorfulLogInterceptor.class.getSimpleName();

    /**
     * 使用调用者代码的 logger
     * 以便使用调用者代码的日志输出级别
     */
    private final boolean useCallerLogger;

    public RestTemplateColorfulLogInterceptor(boolean logTillResponse, boolean useCallerLogger) {
        super(logTillResponse);
        this.useCallerLogger = useCallerLogger;
    }

    /**
     * 左边边界
     */
    private static final String BOUNDARY_LEFT = new ColorString("| ").color(ColorString.CYAN).toString();

    @Override
    protected void logResponse(RestRequestRecord record) {
        ColorStringBuilder builder = new ColorStringBuilder();
        builder
            .newLine()
            .cyan("+---------------------- ")
            .green("Shoulder HTTP Report")
            .cyan(" (" + SELF_CLASS_NAME + ")")
            .cyan(" --------------------- ");

        StackTraceElement stack = LogHelper.findStackTraceElement(RestTemplate.class, "", true);
        // 肯定会有一个，否则不应该触发该方法 null
        if (stack == null) {
            throw new IllegalCallerException("Current StackTrack not contains any RestTemplate's method call!");
        }
        Logger logger = useCallerLogger ? LoggerFactory.getLogger(stack.getClassName()) : log;
        String codeLocation = LogHelper.genCodeLocationLinkFromStack(stack);

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("CodeLocation   : ")
            .append(codeLocation);

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("Aim            : ")
            .append("[").blue(record.getMethod()).append("] ")
            .blue(record.getUrl())
            .append(" ");

        // cost
        long cost = record.getCostTime();
        builder
            .append("(").append(HttpLogHelper.cost(cost)).append(")");

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("requestHeaders : ").append(JsonUtils.toJson(record.getRequestHeaders()))
            .newLine(BOUNDARY_LEFT)
            .lBlue("requestBody    : ").append(record.getRequestBody());

        // ================ response ================

        // code
        int code = record.getStatusCode();
        String httpStatus = String.valueOf(code);
        String codeDescription = record.getStatusText();
        int color = HttpLogHelper.httpStatusColor(httpStatus);
        String tip =
            // 成功
            httpStatus.startsWith("2") ? "√" :
                // 服务器出错
                httpStatus.startsWith("5") ? "×" :
                    // 客户端出错
                    httpStatus.startsWith("4") ? "X" : "";
        builder
            .newLine(BOUNDARY_LEFT)
            .color(tip, color)
            .append(" ")
            .color(httpStatus, color)
            .append(" [").color(codeDescription, color).append("]");

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("responseHeaders: ").append(JsonUtils.toJson(record.getResponseHeaders()))
            .newLine(BOUNDARY_LEFT)
            .lBlue("responseBody   : ").append(record.getResponseBody());

        builder.newLine()
            .cyan("+---------------------------------------------------------------------------------------");

        logger.debug(builder.toString());
    }

}
