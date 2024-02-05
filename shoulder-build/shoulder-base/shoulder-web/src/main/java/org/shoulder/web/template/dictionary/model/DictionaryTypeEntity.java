package org.shoulder.web.template.dictionary.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.io.Serializable;

/**
 * 字典
 *
 * @param <ID> 字典表 、 字典项表主键类型
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryTypeEntity<ID extends Serializable>
    extends BizEntity<ID>
    implements DictionaryType {

    /**
     * 字典项类型（int / String ...）
     */
    //private String valueType;

    /**
     * 展示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 排序
     */
    @TableField(value = DataBaseConsts.COLUMN_DISPLAY_ORDER)
    protected Integer displayOrder;

    /**
     * source
     */
    @TableField("source")
    private String source;

    @Override
    public String getCode() {
        return getBizId();
    }

    /*@Override
    public String getDictionaryType() {
        return Dictionary.super.getDictionaryType();
    }

    @Override
    public String getObjectType() {
        return super.getObjectType();
    }*/

    public static DictionaryItemEntity of(DictionaryItem source) {
        // core.model -> 基于存储的 model
        DictionaryItemEntity entity = new DictionaryItemEntity<>();
        // to confirm dictionaryType.code
        entity.setDictionaryType(source.getDictionaryType());
        entity.setBizId(source.getItemId().toString());
        entity.setName(source.getName());
        entity.setDisplayName(source.getDisplayName());
        entity.setDisplayOrder(source.getDisplayOrder());
        entity.setDescription(source.getDescription());
        return entity;
    }

    @Override
    public String getObjectType() {
        // 这里如果过长，数据库保存时可能报错，强烈建议实现该接口
        return "objectType.shoulder.dictionary";
    }

    @Override
    public String getObjectName() {
        return displayName;
    }

}
