package org.shoulder.data.mybatis.template.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 数据持久层
 *
 * @param <ENTITY> Entity
 * @author lym
 */
public interface BaseMapper<ENTITY extends BaseEntity<? extends Serializable>> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<ENTITY> {


    /**
     * 根据 bizId 查询
     *
     * @param bizId bizId
     * @return entity
     */
    ENTITY selectByBizId(String bizId);

    /**
     * 查询（根据 bizId 批量查询）
     *
     * @param bizIdList bizId 列表(不能为 null 以及 empty)
     * @return entity
     */
    List<ENTITY> selectBatchBizIds(@Param(Constants.COLLECTION) Collection<String> bizId);


    /**
     * 锁定一条记录
     *
     * @param bizId bizId
     * @return 实体
     */
    ENTITY selectForUpdateByBizId(String bizId);

    /**
     * 锁定一条记录
     *
     * @param id 主键
     * @return 实体
     */
    ENTITY selectForUpdateById(Serializable id);

    /**
     * 锁定多条记录
     *
     * @param ids 主键
     * @return 实体
     */
    List<ENTITY> selectBatchForUpdateByIds(List<? extends Serializable> ids);

    /**
     * 锁定多条记录
     *
     * @param bizIds bizId
     * @return 实体
     */
    List<ENTITY> selectBatchForUpdateByBizIds(Collection<? extends String> bizIds);

    // --------------


    /**
     * 根据 bizId 修改
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int updateByBizId(@Param(Constants.ENTITY) ENTITY entity);

    /**
     * 根据 id 全量修改所有包含的字段，与 updateById 区别：包括为 null 的字段，表示设置这个字段为 NULL
     * 不常用
     *
     * @param entity 实体
     * @return 修改数量
     */
    int updateAllFieldsById(@Param(Constants.ENTITY) ENTITY entity);

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
    int deleteInLogicByBizId(ENTITY entity);

    /**
     * 逻辑删除，根据 id 进行
     *
     * @param entityList 实体列表
     * @return 影响行数
     */
    int deleteInLogicByBizIdList(Collection<? extends ENTITY> entityList);

}
