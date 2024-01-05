package org.shoulder.ext.config.usecase;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.ext.config.domain.ConfigField;

/**
 * @author lym
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionConfig {

    @NotNull
    @ConfigField(indexKey = true)
    String region;

}
