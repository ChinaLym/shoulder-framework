package org.shoulder.core.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 树状数据
 *
 * @author lym
 */
@Data
public class TreeNodeDTO implements Serializable {

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
    private Integer namePathLevel;
    /**
     * 路径
     */
    private String path;
    /**
     * 排序
     */
    private Integer disOrder;
    /**
     * 图标
     */
    private String icon;
    /**
     * 权限
     */
    private Boolean auth;
    /**
     * 左子
     */
    private Boolean leaf;
    /**
     * 勾选框禁用状态
     */
    private Boolean chkDisabled;
    /**
     * 是否已经展开
     */
    private Boolean open;

}
