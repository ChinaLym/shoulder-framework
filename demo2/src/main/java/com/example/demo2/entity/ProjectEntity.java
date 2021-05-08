package com.example.demo2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

/**
 * 项目实体
 *
 * @author lym
 * @see BaseEntity 中提供了一些基础的字段：id、createTime、creator、updateTime、modifier
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("tb_project")
public class ProjectEntity extends BaseEntity<Long> {

    @TableField(value = "name", fill = FieldFill.INSERT)
    private String name;

    private static final long serialVersionUID = 1L;

}