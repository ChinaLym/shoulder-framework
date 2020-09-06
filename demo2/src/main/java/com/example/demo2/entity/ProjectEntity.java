package com.example.demo2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.base.entity.RecordEntity;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目实体
 *
 * @author lym
 * @see VariableRecordEntity 中提供了一些基础的字段：id、createTime、creator、updateTime、modifier
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("tb_project")
public class ProjectEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(message = "id can't be null", groups = RecordEntity.Update.class)
    private Long id;

    @TableField(value = "name", fill = FieldFill.INSERT)
    private String name;

    @TableField(value = "crateTime", fill = FieldFill.INSERT)
    private Date crateTime;

    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @TableField(value = "updateTime", fill = FieldFill.INSERT)
    private Date updateTime;

    @TableField(value = "modifier", fill = FieldFill.INSERT)
    private Long modifier;

    private static final long serialVersionUID = 1L;

}