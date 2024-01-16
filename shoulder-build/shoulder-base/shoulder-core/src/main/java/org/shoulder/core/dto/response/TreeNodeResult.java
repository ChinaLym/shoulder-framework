package org.shoulder.core.dto.response;

import lombok.Data;
import org.shoulder.core.dto.ToStringObj;

/**
 * 树状数据
 *
 * @author lym
 */
@Data
public class TreeNodeResult extends ToStringObj {

    private static final long serialVersionUID = -3515352250874606693L;
    /**
     * 数据标识
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 父节点名称
     */
    private String parentId;
    /**
     * 全路径
     */
    private String namePath;
    /**
     * 第几层
     */
    private int namePathLevel;
    /**
     * 路径
     */
    private String path;
    /**
     * 排序
     */
    private int order;
    /**
     * 图标
     */
    private String icon;
    /**
     * 是否有权限
     */
    private boolean auth;
    /**
     * 是否为叶子节点
     */
    private boolean leaf;
    /**
     * 是否启用勾选框
     */
    private boolean checkBox;
    /**
     * 是否已经展开
     */
    private boolean open;

}
