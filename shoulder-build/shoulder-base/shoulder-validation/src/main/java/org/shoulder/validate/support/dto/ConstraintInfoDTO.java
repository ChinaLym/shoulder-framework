package org.shoulder.validate.support.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 检验约束信息 DTO
 *
 * @author lym
 */
@Data
@ToString
@Accessors(chain = true)
public class ConstraintInfoDTO implements Serializable {

    @Serial private static final long   serialVersionUID = -967701495315647751L;
    /**
     * 约束类型，如 NotNull Pattern
     */
    private                      String type;

    /**
     * 约束属性，如 maxLength 2 maxLength 5 Pattern xxx
     */
    private Map<String, Object> attributes;
}
