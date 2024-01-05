package org.shoulder.ext.config.provider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 配置记录
 *
 * @author lym
 */
@Data
public class ConfigItemDTO implements Serializable {

    private static final long serialVersionUID = 8298444466875922486L;
    /**
     * 主键
     */
    @NotNull
    @Length(min = 32, max = 32, message = "bizId must be 32 digits.")
    private String bizId;

    /**
     * 版本号
     */
    @NotNull
    private Integer version;

    /**
     * 操作结果
     */
    private Boolean success;

}
