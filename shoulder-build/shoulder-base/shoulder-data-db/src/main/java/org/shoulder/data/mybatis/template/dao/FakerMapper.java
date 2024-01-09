package org.shoulder.data.mybatis.template.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 仅仅为了兼容接口，复用 controller 、 service，实际不实现未使用功能
 * 若要使用 CRUD Controller 则至少实现
 * insert
 * updateById updateAllById
 * selectById selectList selectPage
 * deleteById deleteBatchIds
 *
 * @author lym
 */
public interface FakerMapper<ENTITY extends BaseEntity<? extends Serializable>> extends BaseMapper<ENTITY> {

    // ========================= atLatest impl for crudController ==========================

    @Override
    default int insert(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default int updateById(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default int updateAllFieldsById(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default ENTITY selectById(Serializable id) {
        throw createNotSupportException();
    }

    @Override
    default List<ENTITY> selectList(Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    @Override
    default <E extends IPage<ENTITY>> E selectPage(E page, Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    @Override
    default int deleteById(Serializable id) {
        throw createNotSupportException();
    }

    default int deleteBatchIds(Collection<?> idList) {
        throw createNotSupportException();
    }

    // ========================= All ==========================

    @Override
    default ENTITY selectByBizId(String bizId) {
        throw createNotSupportException();
    }

    @Override
    default List<ENTITY> selectBatchBizIds(Collection<String> bizId) {
        throw createNotSupportException();
    }

    @Override
    default ENTITY selectForUpdateByBizId(String bizId) {
        throw createNotSupportException();
    }

    @Override
    default ENTITY selectForUpdateById(Serializable id) {
        throw createNotSupportException();
    }

    @Override
    default int updateByBizId(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default int insertBatch(List<ENTITY> dictionaryEntities) {
        throw createNotSupportException();
    }

    @Override
    default int deleteInLogicById(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default int deleteInLogicByIdList(List<? extends Serializable> idList) {
        throw createNotSupportException();
    }

    @Override
    default int deleteInLogicByBizId(ENTITY entity) {
        throw createNotSupportException();
    }

    @Override
    default int deleteInLogicByBizIdList(Collection<? extends ENTITY> entityList) {
        throw createNotSupportException();
    }

    @Override
    default int deleteByMap(Map<String, Object> columnMap) {
        throw createNotSupportException();
    }

    @Override
    default int delete(Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }


    @Override
    default int update(ENTITY entity, Wrapper<ENTITY> updateWrapper) {
        throw createNotSupportException();
    }

    @Override
    default List<ENTITY> selectBatchIds(Collection<? extends Serializable> idList) {
        throw createNotSupportException();
    }

    @Override
    default List<ENTITY> selectByMap(Map<String, Object> columnMap) {
        throw createNotSupportException();
    }

    @Override
    default ENTITY selectOne(Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    default Long selectCount(Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    @Override
    default List<Map<String, Object>> selectMaps(Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    @Override
    default <E extends IPage<Map<String, Object>>> E selectMapsPage(E page, Wrapper<ENTITY> queryWrapper) {
        throw createNotSupportException();
    }

    default BaseRuntimeException createNotSupportException() {
        return new BaseRuntimeException(CommonErrorCodeEnum.DEPRECATED_NOT_SUPPORT);
    }

}
