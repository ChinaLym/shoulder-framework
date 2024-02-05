package org.shoulder.http.interceptor;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.core.context.AppInfo;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 为 RestTemplate 增加记录日志的能力
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数（默认最多记录2048）、返回值（默认最多记录2048）
 * 注意：由于这里类似于 MVC 的过滤器，而不是拦截器，因此最先执行的的拦截器需要保证 body 可重复读，否则将无法重复读取 body
 *
 * @author lym
 * @see BufferingClientHttpRequestFactory 既然要读取记录日志，故 Response Body 必须使用带缓存，而非默认地只读一次
 */
public abstract class BaseRestTemplateLogInterceptor implements ClientHttpRequestInterceptor, Ordered {

    protected static final boolean LOG_TILL_RESPONSE_DEFAULT = true;

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    /**
     * 等待响应返回后再进行统一记录（分开打印会导致请求日志和响应日志不在一起，由于RestTemplate是阻塞的，故在请求中，
     * 当前线程也不会打印其他日志，默认打印在一起，方便查看）。
     * tip:若请求过慢可能导致日志迟迟不打印
     */
    private final boolean logTillResponse;

    public BaseRestTemplateLogInterceptor() {
        this(LOG_TILL_RESPONSE_DEFAULT);
    }

    public BaseRestTemplateLogInterceptor(boolean logTillResponse) {
        this.logTillResponse = logTillResponse;
    }

    @Override
    @Nonnull
    public ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body,
                                        @Nonnull ClientHttpRequestExecution execution) throws IOException {

        StopWatch stopWatch = new StopWatch();
        RestRequestRecord record = RestRequestRecord.builder()
            // request
            .method(request.getMethod().name())
            .url(request.getURI().toString())
            .requestHeaders(request.getHeaders())
            .requestBody(new String(body, AppInfo.charset()))
            .build();

        if (!logTillResponse) {
            logRequest(record);
        }

        stopWatch.start();
        ClientHttpResponse response = execution.execute(request, body);

        stopWatch.stop();

        boolean needLogBody = needLogBody(response);
        String bodyStr = needLogBody ? readBody(response) : "response.Content-Type not readable, " +
            "default support 'json/xml/plain' only";

        record.setCostTime(stopWatch.getLastTaskTimeMillis())
            .setStatusCode(response.getRawStatusCode())
            .setStatusText(response.getStatusText())
            .setResponseHeaders(response.getHeaders())
            .setResponseBody(bodyStr);

        logResponse(record);

        return response;
    }

    /**
     * 默认记录日志的优先级最高
     * 也可能有更高的，如监控。一般推荐 日志|监控 ＜ 安全 ＜ 框架功能 ＜ 用户自定义业务拦截器
     */
    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    /**
     * 是否要记录响应体
     */
    protected boolean needLogBody(@Nonnull ClientHttpResponse response) {
        boolean needLogBody = false;
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null) {
            String contentTypeStr = contentType.getSubtype();
            // 只记录 json、xml、plain 类型，跳过图片、文件这些
            if (contentTypeStr.contains("json") || contentTypeStr.contains("xml") || contentTypeStr.contains("plain")) {
                needLogBody = true;
            }
        }
        return needLogBody;
    }


    /**
     * 记录请求日志
     *
     * @param record 本次请求详情
     */
    protected void logRequest(RestRequestRecord record) {
        // 一般接口调用都是打在一起的，故不需要实现
    }

    /**
     * 记录响应日志
     *
     * @param record 本次请求详情
     */
    protected abstract void logResponse(RestRequestRecord record);

    private String readBody(ClientHttpResponse response) throws IOException {
        StringBuilder resBody = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(),
            AppInfo.charset()))) {

            String line = bufferedReader.readLine();
            while (line != null) {
                resBody.append(line);
                line = bufferedReader.readLine();
            }
        }
        return resBody.toString();
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @SuppressWarnings("rawtypes")
    protected static class RestRequestRecord {
        private String method;
        private String url;
        private HttpHeaders requestHeaders;
        private String requestBody;

        private int statusCode;
        private String statusText;
        private HttpHeaders responseHeaders;
        private String responseBody;

        private long costTime;
    }
}
