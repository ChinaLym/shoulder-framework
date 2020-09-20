package org.shoulder.http.interceptor;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ColorString;
import org.shoulder.core.util.ColorStringBuilder;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.PrintUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 为 RestTemplate 增加记录日志的能力【开发态】
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数、返回值
 *
 * @author lym
 */
public class RestTemplateColorfulLogInterceptor extends BaseRestTemplateLogInterceptor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SELF_CLASS_NAME = RestTemplateColorfulLogInterceptor.class.getSimpleName();

    /**
     * 使用调用者代码的 logger
     * 以便使用调用者代码的日志输出级别
     */
    private final boolean useCallerLogger;

    public RestTemplateColorfulLogInterceptor(boolean useCallerLogger) {
        this(useCallerLogger, BaseRestTemplateLogInterceptor.LOG_TILL_RESPONSE_DEFAULT);
    }

    public RestTemplateColorfulLogInterceptor(boolean useCallerLogger, boolean logTillResponse) {
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
            .yellow("Shoulder HTTP Report", ColorString.Style.NORMAL, true)
            .cyan(" (" + SELF_CLASS_NAME + ")")
            .cyan(" --------------------- ");

        StackTraceElement stack = PrintUtils.findStackTraceElement(RestTemplate.class.getName(), "");
        // 肯定会有一个，否则不应该触发该方法 null
        if (stack == null) {
            throw new IllegalCallerException("Current StackTrack not contains any RestTemplate's method call!");
        }
        Logger logger = useCallerLogger ? LoggerFactory.getLogger(stack.getClassName()) : log;
        String codeLocation = PrintUtils.fetchCodeLocation(stack);

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
        String costStr = String.valueOf(cost);
        int color = cost < 200 ? ColorString.GREEN : cost < 1000 ? ColorString.YELLOW : ColorString.RED;
        builder
            .append("(").color(costStr, color).color("ms", color).append(")");

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("requestHeaders : ").append(JsonUtils.toJson(record.getRequestHeaders()))
            .newLine(BOUNDARY_LEFT)
            .lBlue("requestBody    : ").append(JsonUtils.toJson(record.getRequestBody()));

        // ================ response ================

        // code
        int code = record.getStatusCode();
        String codeStr = String.valueOf(code);
        String codeDesc = record.getStatusText();
        color =
            // 成功
            codeStr.startsWith("2") ? ColorString.GREEN :
                // 服务器出错
                codeStr.startsWith("5") ? ColorString.BLUE :
                    // 客户端出错
                    codeStr.startsWith("4") ? ColorString.RED : ColorString.YELLOW;
        String tip =
            // 成功
            codeStr.startsWith("2") ? "√" :
                // 服务器出错
                codeStr.startsWith("5") ? "×" :
                    // 客户端出错
                    codeStr.startsWith("4") ? "X" : "";
        builder
            .newLine(BOUNDARY_LEFT)
            .color(tip, color)
            .append(" ")
            .color(codeStr, color)
            .append(" [").color(codeDesc, color).append("]");

        builder
            .newLine(BOUNDARY_LEFT)
            .lBlue("responseHeaders: ").append(JsonUtils.toJson(record.getResponseHeaders()))
            .newLine(BOUNDARY_LEFT)
            .lBlue("responseBody   : ").append(JsonUtils.toJson(record.getResponseBody()));

        builder.newLine()
            .cyan("+------------------------------------------------------------------");

        logger.debug(builder.toString());
    }

}
