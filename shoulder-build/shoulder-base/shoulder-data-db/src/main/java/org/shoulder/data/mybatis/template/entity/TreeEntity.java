package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.data.constant.DataBaseConsts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形实体 id createTime updateTime creator modifier name parentId sortNo
 * 举例：组织类、位置类（）
 * 第一个泛型为 id 类型，第二个泛型通常为自身类型
 *
 * 【本类结构】邻接表模型 Adjacency List Model-PRO（存储信息精简；仅保存父子节点关系，ext：深度、根id）；优点：结构简单、存储成本低、CRUD简单；缺点：层级深时候，查询/删除当前节点以及子节点时可能触发递归
 *      适用情况：查询整树或逐级查看；深度有限且较小；插入/删除只操作叶子节点，无删除子树，不涉及子树的子树查询、移动【简单查，插入/删除只操作叶子，结构简单】
 *      另外改良版可以添加 rootId、depth字段优化部分查询。
 * 知识拓展：
 * 【嵌套集模型】Nested Set Model，存储左子，右子id，能够方便的查询深层的树形结构所有子节点，查询性能最好，但所有的写操作都非常复杂（插入、写，修改，移动等）
 *      适用情况：查询为主，查询时需要查询整个树/子树结构，甚至更复杂的查询；极少修改，适用于静态的菜单，数据结构。【查效率好，插入、删除、移动麻烦，结构复杂】
 * 【物理路径模型】Materialized Path Model 每级数据存储 level1.class_2.type3 等，会有冗余空间消耗（存储完整父路径）
 *      适用情况：插入修改多，查询一般多，需要路径匹配查询，不会有复杂的查询。【查效率较好，插入、删除都好、移动麻烦，结构简单，存储空间消耗大】
 * 【闭包模型】Closure Table Model（存储信息全面，写扩散代替读扩散），引入新一张关系表， parentId、child_id、distance 记录所有节点关系；优点：可以高性能支撑任意深度查询、快速获取任意节点的树结构、祖先路径、任意两个节点的距离、跳级查看等复杂查询；缺点：多一张表；更多的关系存储写入成本；节点移动时要更新所有父关系、所有子关系、所有子的父子关系（归级别更新）；
 *      适用情况：深度无限制且可能很多；需要查询整个树结构查询；读多写少；节点移动少
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class TreeEntity<ID extends Serializable> extends Entity<ID> {

    /**
     * 父节点ID，允许为 null
     */
    @TableField(value = DataBaseConsts.COLUMN_PARENT_ID)
    protected ID parentId;

    /**
     * 树形深度
     */
    //@TableField(value = DataBaseConsts.COLUMN_DEPTH)
    //protected Integer depth;

    /**
     * 子节点
     */
    @TableField(exist = false)
    protected List<TreeEntity<ID>> children;

    /**
     * 初始化子类
     */
    public void loadChildren() {
        if (getChildren() == null) {
            this.setChildren(new ArrayList<>());
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
