package org.shoulder.web.template.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.shoulder.core.dto.ToStringObj;

import java.util.Date;

@Data
public class TagDTO extends ToStringObj {

    private static final long serialVersionUID = 1L;

    /**
     * id (long)
     */
    @Schema(description = "唯一标识符", example = "1")
    protected String id;

    /**
     * bizId
     */
    @Schema(description = "租户标识", example = "大陆版")
    protected String tenant;

    /**
     * bizId
     */
    @Schema(description = "业务标识", example = "淘宝")
    protected String bizId;

    /**
     * name
     */
    @Schema(description = "标签名称", example = "标签A")
    protected String name;

    /**
     * 创建时间
     */
    @Schema(description = "标签创建时间", type = "string", format = "date-time", example = "2023-0½-01T00:00:00Z")
    protected Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "标签更新时间", type = "string", format = "date-time", example = "2023-01-02T00:00:00Z")
    protected Date updateTime;

    /**
     * 数据版本
     */
    @Schema(description = "数据版本号", example = "1", type = "integer")
    protected Integer version;

    /**
     * 创建人
     */
    @Schema(description = "标签创建者", example = "user1")
    protected String creator;

    /**
     * 更新人
     */
    @Schema(description = "标签最后更新者", example = "user2")
    protected String modifier;

    /**
     * 标签类型
     */
    @Schema(description = "标签类型", example = "人群分类")
    private String type;

    /**
     * 展示名称
     * 页面显示的内容
     */
    @Schema(description = "展示名称（页面显示内容）", example = "技术人")
    private String displayName;

    /**
     * 展示顺序
     */
    @Schema(description = "展示顺序", example = "1", type = "integer")
    private Integer displayOrder;

    /**
     * 图标
     */
    @Schema(description = "标签图标", example = "/tech/java.svg")
    private String icon;

    /**
     * 来源
     */
    @Schema(description = "标签来源", example = "内部AI打标")
    private String source;

    /**
     * 描述
     */
    @Schema(description = "标签描述", example = "用于标识人群，为广告、推荐、营销等业务提供基础能力。")
    private String description;

    /**
     * 数据版本
     */
    @Schema(description = "删除版本号", example = "-10", type = "integer")
    private Long deleteVersion;
}

