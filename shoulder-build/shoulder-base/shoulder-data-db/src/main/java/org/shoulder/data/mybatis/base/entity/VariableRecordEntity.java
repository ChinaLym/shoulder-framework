package org.shoulder.data.mybatis.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 可变记录型实体: 带有 id、创建时间、创建者、最后修改时间、最后修改者。
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class VariableRecordEntity<T> extends RecordEntity<T> {

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    @TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
    protected Long modifier;

    public VariableRecordEntity(T id, LocalDateTime createTime, Long createUser, LocalDateTime updateTime, Long modifier) {
        super(id, createTime, createUser);
        this.updateTime = updateTime;
        this.modifier = modifier;
    }

    public VariableRecordEntity() {
    }

}
