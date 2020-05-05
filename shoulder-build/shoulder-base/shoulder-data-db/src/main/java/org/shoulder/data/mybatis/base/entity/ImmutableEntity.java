package org.shoulder.data.mybatis.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.time.LocalDateTime;

/**
 * 不可变的实体，对应数据库中不可变或只删不改的记录。如日志，多对多关联表(角色权限)
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ImmutableEntity<ID> {

    @TableId(value = "id", type = IdType.INPUT)
    @NotNull(message = "id can't be null", groups = ImmutableEntity.Update.class)
    protected ID id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    @TableField(value = "creator", fill = FieldFill.INSERT)
    protected Long creator;

    /**
     * 保存和缺省验证组
     */
    public interface Save extends Default {

    }

    /**
     * 更新和缺省验证组
     */
    public interface Update extends Default {

    }
}
