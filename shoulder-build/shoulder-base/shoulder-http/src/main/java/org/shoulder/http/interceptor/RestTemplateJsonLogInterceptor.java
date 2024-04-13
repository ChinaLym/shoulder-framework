package org.shoulder.http.interceptor;

import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.JsonUtils;

/**
 * 为 RestTemplate 增加记录日志的能力
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数（默认最多记录2048）、返回值（默认最多记录2048）
 *
 * @author lym
 */
public class RestTemplateJsonLogInterceptor extends BaseRestTemplateLogInterceptor {

    private static final Logger log = AppLoggers.APP_INTEGRATION;

    public RestTemplateJsonLogInterceptor(boolean logTillResponse) {
        super(logTillResponse);
    }

    @Override
    protected void logResponse(RestRequestRecord record) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.info(JsonUtils.toJson(record));
    }

}
