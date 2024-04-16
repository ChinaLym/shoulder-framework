package org.shoulder.core.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.shoulder.core.dto.ToStringObj;
import org.springframework.http.MediaType;

/**
 * 树状数据
 *
 * @author lym
 */
@Data
@Schema(description = "TreeNodeResult 树形结构返回形式", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class TreeNodeResult extends ToStringObj {

    private static final long serialVersionUID = -3515352250874606693L;

    /**
     * 数据标识
     */
    @Schema(description = "数据id", example = "node1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 名称
     */
    @Schema(description = "节点名称", example = "节点1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 父节点名称
     */
    @Schema(description = "父节点名称", example = "父节点1")
    private String parentId;

    /**
     * 全路径
     */
    @Schema(description = "节点全路径", example = "/parent/node1")
    private String namePath;

    /**
     * 第几层
     */
    @Schema(description = "节点层级", type = "integer", example = "1")
    private int namePathLevel;

    /**
     * 路径
     */
    @Schema(description = "节点路径", example = "/node1")
    private String path;

    /**
     * 排序
     */
    @Schema(description = "节点排序顺序", type = "integer", example = "1")
    private int order;

    /**
     * 图标
     */
    @Schema(description = "节点图标", example = "/tree/icon/home.png")
    private String icon;

    /**
     * 是否有权限
     */
    @Schema(description = "用户是否有访问该节点的权限", type = "boolean", example = "true")
    private boolean auth;

    /**
     * 是否为叶子节点
     */
    @Schema(description = "节点是否为叶子节点（无子节点）", type = "boolean", example = "false")
    private boolean leaf;

    /**
     * 是否启用勾选框
     */
    @Schema(description = "节点是否显示勾选框", type = "boolean", example = "true")
    private boolean checkBox;

    /**
     * 是否已经展开
     */
    @Schema(description = "节点是否默认展开", type = "boolean", example = "false")
    private boolean open;


}
