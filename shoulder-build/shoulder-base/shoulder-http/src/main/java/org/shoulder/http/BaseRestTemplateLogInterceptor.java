package org.shoulder.http;

import lombok.Builder;
import lombok.Data;
import lombok.extern.shoulder.SLog;
import org.shoulder.core.context.AppInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 为 RestTemplate 增加记录日志的能力
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数（默认最多记录2048）、返回值（默认最多记录2048）
 *
 * @author lym
 */
@SLog
public abstract class BaseRestTemplateLogInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ClientHttpResponse response = execution.execute(request, body);

        stopWatch.stop();

        boolean needLogBody = needLogBody(request);
        String bodyStr = needLogBody ? readBody(response) : "contentType not readable";

        RestRequestRecord record = RestRequestRecord.builder()
            .costTime(stopWatch.getLastTaskTimeMillis())
            // request
            .method(request.getMethodValue())
            .url(request.getURI().toString())
            .requestHeaders(request.getHeaders())
            .requestBody(new String(body, AppInfo.charset()))
            // response
            .statusCode(response.getRawStatusCode())
            .statusText(response.getStatusText())
            .responseBody(bodyStr)
            //.responseHeaders()


            .build();

        log.debug(buildHttpLog(record));

        return response;
    }

    /**
     * 是否要记录响应体
     */
    protected boolean needLogBody(@NonNull HttpRequest request) {
        boolean needLogBody = false;
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType != null) {
            String contentTypeStr = contentType.toString();
            // 只记录 json、xml、plain 类型，跳过图片、文件这些
            if (contentTypeStr.contains("json") || contentTypeStr.contains("xml") || contentTypeStr.contains("plain")) {
                needLogBody = true;
            }
        }
        return needLogBody;
    }

    /**
     * 组装日志
     *
     * @param record 本次请求详情
     * @return 带记录的日志
     */
    protected abstract String buildHttpLog(RestRequestRecord record);

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
