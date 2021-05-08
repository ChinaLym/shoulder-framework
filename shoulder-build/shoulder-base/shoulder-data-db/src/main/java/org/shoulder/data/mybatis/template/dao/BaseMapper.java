package org.shoulder.data.mybatis.template.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

import java.util.List;

/**
 * 数据持久层
 *
 * @param <ENTITY> Entity
 * @author lym
 */
public interface BaseMapper<ENTITY extends BaseEntity<?>> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<ENTITY> {

    /**
     * 根据 id 全量修改所有字段，与 updateById 区别：包括为 null 的字段，表示设置这个字段为 NULL
     *
     * @param entity 实体
     * @return 修改数量
     */
    int updateAllById(@Param(Constants.ENTITY) ENTITY entity);

    /**
     * 批量插入（所有字段）
     *
     * @param entityList 实体集合
     * @return 插入数量
     */
    int insertBatch(List<ENTITY> entityList);

    /**
     * 批量插入（所有字段）
     *
     * @param entityList 实体集合
     * @return 插入数量
     */
    int deleteInLogicById(ENTITY entityList);

}
