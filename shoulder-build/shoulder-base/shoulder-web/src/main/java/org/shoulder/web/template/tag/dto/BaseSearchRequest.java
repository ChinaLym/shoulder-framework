package org.shoulder.web.template.tag.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.dto.ToStringObj;

import java.io.Serial;
import java.util.Map;

@Getter @Setter public class BaseSearchRequest extends ToStringObj {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    @NotBlank private String bizType;

    /**
     * 搜索框输入的内容
     * 若为空则查全部
     * 不为空作为模糊搜索条件
     */
    @NotBlank private String searchContent;

    /**
     * 业务上过滤条件
     */
    private Map<String, String> condition;

    @DecimalMax("1000") private Integer limit;

}
