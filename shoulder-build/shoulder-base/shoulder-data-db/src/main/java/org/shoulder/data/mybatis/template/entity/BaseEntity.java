package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.core.model.Operable;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.config.handler.ModelMetaObjectHandler;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记录型实体: 带有 id、创建时间、最后修改时间
 *
 * @author lym
 * @see SqlCondition 注解加条件
 * @see ModelMetaObjectHandler 自动填充创建、修改时间、创建人、更新人、版本号、是否删除等
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity<ID extends Serializable> implements Operable, Serializable {

    @Serial
    private static final long serialVersionUID = -7975111840965852408L;

    @TableId(value = DataBaseConsts.COLUMN_ID, type = IdType.INPUT)
//    @NotNull(message = "id can't be null", groups = Update.class)
    protected ID id;

    @TableField(value = DataBaseConsts.COLUMN_CREATE_TIME, fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    protected LocalDateTime createTime;

    @TableField(value = DataBaseConsts.COLUMN_UPDATE_TIME, fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public String getObjectId() {
        return String.valueOf(id);
    }

    @Override
    public String getObjectName() {
        return getObjectId();
    }

}
