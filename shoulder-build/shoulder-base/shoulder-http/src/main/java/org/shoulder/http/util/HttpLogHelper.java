package org.shoulder.http.util;

import org.shoulder.core.log.beautify.ColorString;
import org.shoulder.core.log.beautify.ColorStringBuilder;

import java.util.Map;

/**
 * 请求日志美化
 *
 * @author lym
 */
public class HttpLogHelper {


    /**
     * 组装颜色字符串
     *
     * @param cost 毫秒
     * @return 颜色字符串
     */
    public static ColorString cost(long cost) {
        return new ColorString(cost + "ms").color(costColor(cost));
    }

    /**
     * 请求花费时间的颜色
     *
     * @param cost 毫秒
     * @return 颜色
     */
    public static int costColor(long cost) {
        return cost < 200 ? ColorString.GREEN :
            cost < 1000 ? ColorString.YELLOW :
                ColorString.RED;
    }

    /**
     * 响应状态颜色
     *
     * @param httpStatus 状态
     * @return 颜色
     */
    public static int httpStatusColor(String httpStatus) {
        // 成功
        return httpStatus.startsWith("2") ? ColorString.GREEN :
            // 服务器出错
            httpStatus.startsWith("5") ? ColorString.BLUE :
                // 客户端出错
                httpStatus.startsWith("4") ? ColorString.RED : ColorString.YELLOW;
    }

    /**
     * 添加请求头信息
     *
     * @param builder 彩色字符串
     * @param headers 请求头
     */
    public static void appendHeader(ColorStringBuilder builder, Map<String, String> headers) {
        headers.forEach((headerName, headerValue) -> builder
            .newLine().tab()
            .lGreen(headerName)
            .tab()
            .blue(": ")
            .cyan(headerValue));
    }

}
