package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
public class LogicDeleteEntity<ID extends Serializable> extends Entity<ID> implements ILogicDeleteEntity {

    /**
     * 删除标记
     * 0 未删除
     * n 删除时设置为当前时间戳
     */
    @TableField(value = "delete_version", fill = FieldFill.INSERT)
    @TableLogic(value = "0")
    private Long deleteVersion;

    public LogicDeleteEntity() {
    }

    public LogicDeleteEntity(ID id, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long modifier) {
        super(id, createTime, updateTime, createUser, modifier);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
