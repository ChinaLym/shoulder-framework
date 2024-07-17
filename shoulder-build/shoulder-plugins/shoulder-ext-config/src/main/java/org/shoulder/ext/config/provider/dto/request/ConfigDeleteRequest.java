package org.shoulder.ext.config.provider.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.shoulder.ext.config.provider.dto.ConfigItemDTO;

import java.io.Serializable;
import java.util.List;

/**
 * @author lym
 */
@Data
public class ConfigDeleteRequest implements Serializable {

    @Serial private static final long serialVersionUID = -3930917032812315390L;

    @NotNull
    @Size(min = 1, max = 500, message = "configItemList size need between 1,500 ")
    @Valid
    private List<ConfigItemDTO> configItemList;

}
