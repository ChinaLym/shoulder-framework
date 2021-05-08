package com.example.demo3.repository;

import com.example.demo3.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;

/**
 * 持久层
 *
 * @author lym
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
