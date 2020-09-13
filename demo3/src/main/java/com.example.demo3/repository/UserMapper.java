package com.example.demo3.repository;

import com.example.demo3.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.base.dao.IBaseRepository;

/**
 * 持久层
 *
 * @author lym
 */
@Mapper
public interface UserMapper extends IBaseRepository<UserEntity> {
}
