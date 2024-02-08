package org.shoulder.autoconfigure.batch;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.batch.config.model.BatchInitializationSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * batch 配置
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = BatchProperties.PREFIX)
public class BatchProperties extends BatchInitializationSettings {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "batch";

    private StorageConfig storage = new StorageConfig();

    @Data
    public static class StorageConfig {
        private String type;
    }

}
