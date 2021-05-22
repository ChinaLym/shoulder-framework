package org.shoulder.ext.config.provider.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字段信息
 *
 * @author lym
 */
@Data
public class ConfigFieldInfoDTO implements Serializable {

    private static final long serialVersionUID = -6199304842326961220L;

    /**
     * 显示顺序
     */
    private Integer order;

    /**
     * 字段名
     * 用于与数据关联
     */
    private String name;

    /**
     * 展示名称
     * 用于前端表格 header/编辑label 字段名展示
     */
    private String displayName;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 是否为索引（非空，必填，无法修改）
     */
    private Boolean index;

    /**
     * 不能为 null / 空串
     */
    private Boolean notEmpty;

    /**
     * 不能全为空字符
     */
    private Boolean notBlank;

    /**
     * 最小长度
     */
    private Integer minLength;

    /**
     * 最大长度
     */
    private Integer maxLength;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 默认值
     */
    private String defaultValue;

}