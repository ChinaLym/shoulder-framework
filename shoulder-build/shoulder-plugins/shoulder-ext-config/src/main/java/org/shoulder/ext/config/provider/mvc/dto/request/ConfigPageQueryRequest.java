package org.shoulder.ext.config.provider.mvc.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lym
 */
@Data
public class ConfigPageQueryRequest implements Serializable {

    private static final long serialVersionUID = -3930917032812315390L;

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