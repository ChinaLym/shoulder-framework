package com.example.demo2.repository;

import com.example.demo2.entity.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.base.dao.IBaseRepository;

/**
 * 持久层
 *
 * @author lym
 */
@Mapper
public interface ProjectMapper extends IBaseRepository<ProjectEntity> {
}
