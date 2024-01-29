package org.shoulder.web.template.tag.dto;

import lombok.Data;
import org.shoulder.core.dto.ToStringObj;

import java.util.Date;

@Data
public class TagDTO extends ToStringObj {

    private static final long serialVersionUID = 1L;

    /**
     * id (long)
     */
    protected String id;

    /**
     * 创建时间
     */
    protected Date createTime;

    /**
     * 更新时间
     */
    protected Date updateTime;

    /**
     * 数据版本
     */
    protected Integer version;

    /**
     * 创建人
     */
    protected String creator;

    /**
     * 更新人
     */
    protected String modifier;

    /**
     * 标签类型
     * * issue
     */
    private String bizType;

    /**
     * 展示名称
     * 页面显示的内容
     */
    private String displayName;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;
}

