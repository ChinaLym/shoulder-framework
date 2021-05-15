package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可变记录型实体: 带有 id、创建时间、最后修改时间、创建者、最后修改者
 * <p>
 * 索引：biz_id - delete_version - version （加 version 主要是为了 lock for update 时候避免锁表）
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class BizEntity<ID extends Serializable> extends LogicDeleteEntity<ID> {

    /**
     * 业务唯一索引键
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 版本号
     */
    @Version
    private int version;

    /**
     * 描述
     */
    @Version
    private String description;

    public BizEntity() {
    }

    public BizEntity(ID id, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long modifier) {
        super(id, createTime, updateTime, createUser, modifier);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
