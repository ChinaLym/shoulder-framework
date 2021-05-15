package org.shoulder.data.mybatis.template.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 数据持久层
 *
 * @param <ENTITY> Entity
 * @author lym
 */
public interface BaseMapper<ENTITY extends BaseEntity<?>> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<ENTITY> {

    /**
     * 锁定一条记录
     *
     * @param id 主键
     * @return 实体
     */
    ENTITY selectForUpdateById(Serializable id);

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
     * 逻辑删除，根据 id 进行
     *
     * @param entity 实体
     * @return 影响行数
     */
    int deleteInLogicById(ENTITY entity);

    /**
     * 逻辑删除，根据 id 进行
     *
     * @param idList idList
     * @return 影响行数
     */
    int deleteInLogicByIdList(List<? extends Serializable> idList);

    /**
     * 逻辑删除，根据 id 进行
     *
     * @param entity 实体
     * @return 影响行数
     */
    int deleteInLogicByBizIndex(ENTITY entity);

    /**
     * 逻辑删除，根据 id 进行
     *
     * @param entityList 实体列表
     * @return 影响行数
     */
    int deleteInLogicByBizIndex(List<ENTITY> entityList);

}
