package org.shoulder.ext.config.provider.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

/**
 * @author lym
 */
@Data
public class ConfigCreateRequest implements Serializable {

    private static final long serialVersionUID = -3930917032812315390L;

    @NotNull
    private String tenant;

    @NotNull
    private String configType;

    @NotNull
    @Size(min = 1)
    private Map<String, String> data;

}