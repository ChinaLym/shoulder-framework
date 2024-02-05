package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.annotation.BizIdSource;
import org.shoulder.data.constant.DataBaseConsts;

import java.io.Serializable;
import java.util.List;

/**
 * 树形 biz 实体： id createTime updateTime creator modifier bizId version deleteVersion name parentId sortNo
 * 举例：组织类、位置类（）
 * 第一个泛型为 id 类型，第二个泛型通常为自身类型
 *
 * 实现类必须有字段标注{@link BizIdSource}供框架自行生成 {@link BizTreeEntity#bizId}，或创建时自行填写，否则创建时失败
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class BizTreeEntity<ID extends Serializable> extends BizEntity<ID> implements ILogicDeleteEntity {

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

}
