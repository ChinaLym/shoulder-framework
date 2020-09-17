package org.shoulder.http;

import org.shoulder.core.util.ColorString;
import org.shoulder.core.util.ColorStringBuilder;

/**
 * 为 RestTemplate 增加记录日志的能力【开发态】
 * // todo 定位代码位置
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数、返回值
 *
 * @author lym
 */
public class RestTemplateColorfulLogInterceptor extends BaseRestTemplateLogInterceptor {

    private static final String LEFT_TABLE_CHAR = "|";

    @Override
    protected String buildHttpLog(RestRequestRecord record) {
        ColorStringBuilder builder = new ColorStringBuilder();
        builder
            .newLine(LEFT_TABLE_CHAR)
            .cyan("--------------------- ")
            .yellow("Shoulder HTTP Report ", ColorString.Style.BOLD, true)
            .cyan(" ---------------------")
            .newLine(LEFT_TABLE_CHAR)
            .lBlue("Aim            : ")
            .append("[").blue(record.getMethod()).append("] ")
            .blue(record.getUrl())
            .append(" ");

        // cost
        long cost = record.getCostTime();
        String costStr = String.valueOf(cost);
        int color = cost < 200 ? ColorString.GREEN : cost < 1000 ? ColorString.YELLOW : ColorString.RED;
        builder
            .append("(").color(costStr, color).color("ms", color).append(")")
            .newLine(LEFT_TABLE_CHAR);

        builder
            .lBlue("requestHeaders : ").append("xxx").newLine(LEFT_TABLE_CHAR)
            .lBlue("requestBody    : ").append("xxx").newLine(LEFT_TABLE_CHAR);

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
            .color(tip, color)
            .append(" ")
            .color(codeStr, color)
            .append(" [").color(codeDesc, color).append("]")
            .newLine(LEFT_TABLE_CHAR);

        builder
            .lBlue("responseHeaders: ").append("xxx").newLine(LEFT_TABLE_CHAR)
            .lBlue("responseBody   : ").append("xxx").newLine(LEFT_TABLE_CHAR);

        builder.cyan("-----------------------------------------------------------------");

        return builder.toString();
    }

}
