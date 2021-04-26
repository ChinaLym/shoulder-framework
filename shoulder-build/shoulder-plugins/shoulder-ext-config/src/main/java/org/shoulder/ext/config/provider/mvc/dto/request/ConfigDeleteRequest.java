package org.shoulder.ext.config.provider.mvc.dto.request;

import lombok.Data;
import org.shoulder.ext.config.provider.mvc.dto.ConfigItemDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author lym
 */
@Data
public class ConfigDeleteRequest implements Serializable {

    private static final long serialVersionUID = -3930917032812315390L;

    @NotNull
    @Size(min = 1, max = 500, message = "configItemList size need between 1,500 ")
    @Valid
    private List<ConfigItemDTO> configItemList;

}