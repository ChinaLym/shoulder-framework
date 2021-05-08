package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;
import org.shoulder.data.constant.DataBaseConsts;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形实体
 * 举例：组织类、位置类（）
 * 第一个泛型为 id 类型，第二个泛型通常为自身类型
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class TreeEntity<ID> extends BaseEntity<ID> {

    /**
     * 名称
     */
    @NotEmpty(message = "label can't be null")
    @Length(max = 255, message = "label length must less than 255")
    @TableField(value = DataBaseConsts.COLUMN_LABEL, condition = SqlCondition.LIKE)
    protected String label;

    /**
     * 父节点ID，允许为 null
     */
    @TableField(value = DataBaseConsts.COLUMN_PARENT_ID)
    protected ID parentId;

    /**
     * 排序
     */
    @TableField(value = DataBaseConsts.COLUMN_SORT_NO)
    protected Integer sortNo;

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
