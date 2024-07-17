package org.shoulder.ext.config.provider.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lym
 */
@Data
public class ConfigPageQueryRequest implements Serializable {

    @Serial private static final long serialVersionUID = -3930917032812315390L;

    @NotNull
    private String tenant;

    @NotNull
    private String configType;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(1000)
    private Integer pageSize;

}
