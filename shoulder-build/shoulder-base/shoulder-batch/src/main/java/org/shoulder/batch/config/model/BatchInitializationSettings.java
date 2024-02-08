package org.shoulder.batch.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * batch 配置
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class BatchInitializationSettings {

    public List<String> exportLocalizeConfigLocations;

    public List<String> exportFileConfigLocations;

}
