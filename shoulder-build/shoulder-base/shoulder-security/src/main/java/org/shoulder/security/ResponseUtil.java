package org.shoulder.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 默认的响应格式
 * {"msg":"xxx"}
 *
 * @author lym
 * @deprecated use RestResult
 */
public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String jsonMsg(String msg) {
        return "{\"msg\":\"" + msg + "\"}";
    }

    /**
     * 写成 json 形式
     *
     * @param obj not null
     */
    public static String jsonMsg(Object obj) throws JsonProcessingException {
        return jsonMsg(objectMapper.writeValueAsString(obj));
    }

    public static String success() {
        return "{\"msg\":\"success\"}";
    }

}
