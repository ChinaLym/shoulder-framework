package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.data.constant.DataBaseConsts;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.time.LocalDateTime;

/**
 * 记录型实体: 带有 id、创建时间、最后修改时间
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity<ID> {

    @TableId(value = DataBaseConsts.COLUMN_ID, type = IdType.INPUT)
    @NotNull(message = "id can't be null", groups = BaseEntity.Update.class)
    protected ID id;

    @TableField(value = DataBaseConsts.COLUMN_CREATE_TIME, fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    @TableField(value = DataBaseConsts.COLUMN_UPDATE_TIME, fill = FieldFill.UPDATE)
    protected LocalDateTime updateTime;

    /**
     * 验证组分组-创建时
     */
    public interface Create extends Default {

    }

    /**
     * 验证组分组-保存时
     */
    public interface Update extends Default {

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
