package org.shoulder.data.mybatis.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static com.baomidou.mybatisplus.annotation.SqlCondition.LIKE;

/**
 * 树形实体
 * 举例：组织类、位置类（）
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class TreeEntity<E, T> extends BaseEntity<T> {

    /**
     * 名称
     */
    @NotEmpty(message = "label can't be null")
    @Length(max = 255, message = "label length must less than 255")
    @TableField(value = "label", condition = LIKE)
    protected String label;

    /**
     * 父ID
     */
    @TableField(value = "parent_id")
    protected T parentId;

    /**
     * 排序
     */
    @TableField(value = "sort")
    protected Integer sort;

    /**
     * 子节点
     */
    @TableField(exist = false)
    protected List<E> children;

}
