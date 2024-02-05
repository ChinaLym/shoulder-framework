package org.shoulder.web.template.dictionary.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.util.Optional;

/**
 * 字典类型
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_dictionary_type")
public class DictionaryTypeEntity
        extends BizEntity<Long>
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
    @TableField(value = "source", updateStrategy = FieldStrategy.NEVER)
    private String source;

    @Override
    public String getCode() {
        return getBizId();
    }

    @Override
    public boolean modifyAble() {
        return true;
    }

    public void setCode(String typeCode) {
        // 这里如果过长，数据库保存时可能报错，强烈建议实现该接口
        setBizId(typeCode);
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
        DictionaryItemEntity entity = new DictionaryItemEntity();
        // to confirm dictionaryType.code
        entity.setDictionaryType(source.getDictionaryType());
        entity.setBizId(Optional.ofNullable(source.getItemId()).map(Object::toString).orElse(null));
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
