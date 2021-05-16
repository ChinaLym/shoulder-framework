package com.example.demo2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.Entity;

import java.time.LocalDateTime;

/**
 * 用户实体，举例：主键为 uuid
 * 自动生成主键、创建者，创建时间，修改者，修改时间
 *
 * @author lym
 * @see Entity 提供了一些基础的字段：id、createTime、creator、updateTime、modifier
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("user_info")
public class UserEntity extends BaseEntity<Long> {

    @TableField(value = "name")
    private String name;

    @TableField(value = "sex")
    private Integer sex;

    @TableField(value = "age")
    private Integer age;

    @TableField(value = "email")
    private String email;

    // todo Date 反序列化失败，暂不提供支持，后续支持
    //  【考虑】LocalDate 支持带时间的，只取日期部分 LocalTime 同理
    @TableField(value = "birth")
    private LocalDateTime birth;

    @TableField(value = "level")
    private Integer level;

    @TableField(value = "group_name")
    private String groupName;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "creator")
    private Long creator;
}
