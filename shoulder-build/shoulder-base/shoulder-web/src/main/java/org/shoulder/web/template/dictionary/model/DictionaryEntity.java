package org.shoulder.web.template.dictionary.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BizTreeEntity;

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
public class DictionaryEntity<ID extends Serializable> extends BizTreeEntity<ID> implements Dictionary<ID> {

    /**
     * 展示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 字典项类型（int / String ...）
     */
    private String type;

    /*@Override
    public String getDictionaryType() {
        return Dictionary.super.getDictionaryType();
    }

    @Override
    public String getObjectType() {
        return super.getObjectType();
    }*/
}
