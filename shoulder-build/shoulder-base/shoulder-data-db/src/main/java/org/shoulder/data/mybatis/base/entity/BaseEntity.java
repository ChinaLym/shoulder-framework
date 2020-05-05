package org.shoulder.data.mybatis.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 基础实体
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class BaseEntity<T> extends ImmutableEntity<T> {

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    @TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
    protected Long modifier;

    public BaseEntity(T id, LocalDateTime createTime, Long createUser, LocalDateTime updateTime, Long modifier) {
        super(id, createTime, createUser);
        this.updateTime = updateTime;
        this.modifier = modifier;
    }

    public BaseEntity() {
    }

}