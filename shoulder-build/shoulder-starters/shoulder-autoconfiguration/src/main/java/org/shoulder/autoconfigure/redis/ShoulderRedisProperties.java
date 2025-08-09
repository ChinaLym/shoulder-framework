package org.shoulder.autoconfigure.redis;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.core.util.AddressUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 实例标识
 * 支持集群时才有效
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = ShoulderRedisProperties.PREFIX)
public class ShoulderRedisProperties {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "redis";


    private EmbeddedRedisProperties embedded = new EmbeddedRedisProperties();

    @Data
    public static class EmbeddedRedisProperties {

        /**
         * 是否要启动嵌入式 redis
         */
        private Boolean active = false;

        /**
         * 绑定 host，默认仅本机可访问，设置为 `0.0.0.0` 则允许其他机器访问（注意同时配置 requirepass xxx 要求连接时使用密码）
         */
        private String bind = AddressUtils.LOCAL_HOST_IP4;

        /**
         * 启动端口号
         */
        private Integer port = 6379;

        /**
         * 启动参数，默认限制使用内存64m
         */
        private String configuration = "maxmemory 64M";
    }

}
