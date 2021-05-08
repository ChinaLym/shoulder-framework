package com.example.demo2.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo2.entity.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 持久层
 *
 * @author lym
 */
@Mapper
public interface ProjectMapper extends BaseMapper<ProjectEntity> {
}
