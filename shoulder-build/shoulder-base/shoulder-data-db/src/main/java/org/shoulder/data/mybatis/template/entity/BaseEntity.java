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
import org.shoulder.validate.groups.Update;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记录型实体: 带有 id、创建时间、最后修改时间
 * todo createTime update 时区问题，update明显落后 8 h
 *
 * @author lym
 * @see SqlCondition 注解加条件
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity<ID extends Serializable> implements Operable {

    @TableId(value = DataBaseConsts.COLUMN_ID, type = IdType.INPUT)
    @NotNull(message = "id can't be null", groups = Update.class)
    protected ID id;

    @TableField(value = DataBaseConsts.COLUMN_CREATE_TIME, fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    @TableField(value = DataBaseConsts.COLUMN_UPDATE_TIME, fill = FieldFill.UPDATE)
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
