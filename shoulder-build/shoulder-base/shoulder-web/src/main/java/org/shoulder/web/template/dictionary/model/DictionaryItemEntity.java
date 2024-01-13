package org.shoulder.web.template.dictionary.model;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.io.Serializable;

/**
 * 字典项
 * itemId 用 bizId 实现
 * @param <ID> 字典表 、 字典项表主键类型
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemEntity<ID extends Serializable> extends BizEntity<ID> implements DictionaryItem<String> {

    /**
     * 字典类型 / 业务类型，关联到 {@link DictionaryEntity}
     */
    @TableField("dictionary_id")
    ID dictionaryId;

    /**
     * 名称
     */
    @NotEmpty(message = "name can't be null")
    @Length(max = 255, message = "name length must less than 255")
    @TableField(value = DataBaseConsts.COLUMN_LABEL, condition = SqlCondition.LIKE)
    protected String name;

    /**
     * 排序
     */
    @TableField(value = DataBaseConsts.COLUMN_SORT_NO)
    protected Integer sortNo;

    /**
     * 展示名称
     */
    @TableField("display_name")
    private String displayName;

    @Override
    public String getItemId() {
        return name;
    }

    @Override
    public Integer getDisplayOrder() {
        return sortNo;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
