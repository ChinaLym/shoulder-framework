package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.data.constant.DataBaseConsts;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可变记录型实体: 带有 id、创建时间、最后修改时间、创建者、最后修改者
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class Entity<ID extends Serializable> extends BaseEntity<ID> {

    @TableField(value = DataBaseConsts.COLUMN_CREATOR, fill = FieldFill.INSERT)
    protected Long creator;

    @TableField(value = DataBaseConsts.COLUMN_MODIFIER, fill = FieldFill.INSERT_UPDATE)
    protected Long modifier;

    public Entity() {
    }

    public Entity(ID id, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long modifier) {
        super(id, createTime, updateTime);
        this.creator = createUser;
        this.modifier = modifier;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
