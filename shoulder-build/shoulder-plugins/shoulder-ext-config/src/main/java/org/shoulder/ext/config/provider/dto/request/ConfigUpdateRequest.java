package org.shoulder.ext.config.provider.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

/**
 * @author lym
 */
@Data
public class ConfigUpdateRequest implements Serializable {

    private static final long serialVersionUID = -3930917032812315390L;

    @NotNull
    @Length(min = 32, max = 32, message = "bizId must be 32 digits.")
    private String bizId;

    @NotNull
    private Integer version;

    @NotNull
    @Size(min = 1)
    private Map<String, String> data;

}