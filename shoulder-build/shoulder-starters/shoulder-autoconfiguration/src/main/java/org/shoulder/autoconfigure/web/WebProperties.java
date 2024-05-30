package org.shoulder.autoconfigure.web;

import lombok.Data;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * web 扩展能力配置
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = WebProperties.PREFIX)
public class WebProperties {

    /**
     * EXT 配置 key 前缀：shoulder.{moduleName}.ext.{functionName}.xxx
     */
    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "web";

    private CommonEndpointProperties commonEndpoint = new CommonEndpointProperties();

    private WebWafProperties waf = new WebWafProperties();

    private WebLogProperties log = new WebLogProperties();

    private Boolean handleGlobalException = Boolean.TRUE;

    private RestResponseProperties restResponse = new RestResponseProperties();

    @Data
    public static class RestResponseProperties {

        /**
         * 是否自动包装为统一响应格式 {"code": xx, "msg":"xxx", data: ... }
         */
        private Boolean autoWrapFormat = Boolean.TRUE;

        /**
         * 跳过自动包装响应的请求路径列表，支持 AntPathMatcher 表达式匹配
         */
        private List<String> skipWrapPathPatterns = new ArrayList<>();

    }

    @Data
    public static class CommonEndpointProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.TRUE;

    }


    @Data
    public static class WebWafProperties {

        private RepeatSubmitProperties repeatSubmit = new RepeatSubmitProperties();
    }

    @Data
    public static class RepeatSubmitProperties {
        private static final String DEFAULT_TOKEN_KEY = "__repeat_token";

        /**
         * whether enable
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * Name of token in Http.header
         */
        private String requestTokenName = DEFAULT_TOKEN_KEY;

        /**
         * Name of token in Server.session
         */
        private String sessionTokenName = DEFAULT_TOKEN_KEY;

    }

    @Data
    static class WebLogProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * true: 请求处理完毕后打印一条日志
         * false：收到请求时打印一条request相关的，处理完毕后打印一条response相关的
         */
        private Boolean mergeReqAndResp = Boolean.TRUE;

        /**
         * ture：使用 LoggerFactory.getLogger(xxxController.class)，会有反射调用
         * false：使用固定的 Logger
         */
        private Boolean useCallerLogger = Boolean.TRUE;

        /**
         * 日志打印格式
         */
        private WebLogType type = WebLogType.COLORFUL;

    }

    public enum WebLogType {

        /**
         * 彩色格式
         */
        COLORFUL,
        /**
         * 压缩的 JSON 格式
         */
        JSON,
        /**
         * 禁止自动打印日志
         */
        DISABLE,
        ;
    }

}
