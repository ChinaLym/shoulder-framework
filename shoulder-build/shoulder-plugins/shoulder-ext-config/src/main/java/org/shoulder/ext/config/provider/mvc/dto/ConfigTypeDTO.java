package org.shoulder.ext.config.provider.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 配置类型
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigTypeDTO implements Serializable {

    private static final long serialVersionUID = -1212800132345290597L;

    private String name;

    private String displayName;

}