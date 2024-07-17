package org.shoulder.web.template.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量查询参数
 *
 * @author lym
 */
@Data
@Schema(description = "字典批量查询参数", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class DictionaryBatchQueryParam implements Serializable {

    @Serial private static final long serialVersionUID = 2773729771586823614L;

    /**
     * 字典类型
     */
    @NotNull
    @Size(max = 20)
    @Schema(description = "字典类型", example = "[\"UserStatus\"]", requiredMode = Schema.RequiredMode.REQUIRED, type = "List<String>", subTypes = {String.class})
    List<String> dictionaryTypeList;

}
