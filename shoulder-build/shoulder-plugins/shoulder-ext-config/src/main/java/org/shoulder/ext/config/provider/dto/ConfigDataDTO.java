package org.shoulder.ext.config.provider.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 配置数据
 *
 * @author lym
 */
@Data
public class ConfigDataDTO implements Serializable {

    @Serial private static final long serialVersionUID = 9040359877503481492L;

    /**
     * 租户信息
     */
    private String tenant;

    /**
     * 配置类信息
     */
    private String configType;

    /**
     * 业务唯一标识
     */
    private String bizId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date lastUpdateTime;

    /**
     * 操作人编码
     */
    private String operatorNo;

    /**
     * 操作人名
     */
    private String operatorName;

    /**
     * 业务数据值
     */
    private Map<String, String> businessValue;

}
