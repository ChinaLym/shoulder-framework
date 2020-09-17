package org.shoulder.http;

import org.shoulder.core.util.JsonUtils;

/**
 * 为 RestTemplate 增加记录日志的能力
 * 记录：方法调用位置、请求方式、地址、请求头、请求参数（默认最多记录2048）、返回值（默认最多记录2048）
 *
 * @author lym
 */
public class RestTemplateJsonLogInterceptor extends BaseRestTemplateLogInterceptor {

    @Override
    protected String buildHttpLog(RestRequestRecord record) {
        return JsonUtils.toJson(record);
    }

}
