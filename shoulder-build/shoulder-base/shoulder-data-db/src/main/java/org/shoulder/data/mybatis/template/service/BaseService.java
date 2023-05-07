package org.shoulder.data.mybatis.template.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.converter.DateConverter;
import org.shoulder.core.dto.request.BasePageQuery;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.data.uid.EntityIdGenerator;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * shoulder 定义的基本服务，提供了增删改查等通用代码
 *
 * @author lym
 */
public interface BaseService<ENTITY extends BaseEntity<? extends Serializable>> extends IService<ENTITY> {

    @Override
    BaseMapper<ENTITY> getBaseMapper();

    ConversionService getConversionService();

    Class<ENTITY> getModelClass();

    EntityIdGenerator getIdGenerator();

    /**
     * 根据id修改 entity 的所有字段，包含 NULL
     *
     * @param entity 实体
     * @return 更新成功
     */
    boolean updateAllById(ENTITY entity);

    /**
     * 根据 id 锁定
     *
     * @param id id
     * @return 非空，已经存在；空：不存在
     */
    default ENTITY lockById(Serializable id) {
        return getBaseMapper().selectForUpdateById(id);
    }

    /**
     * 根据 id 锁定
     *
     * @param id id
     * @return 非空，已经存在；空：不存在
     */
    default List<ENTITY> lockByIds(List<? extends Serializable> id) {
        return getBaseMapper().selectBatchForUpdateByIds(id);
    }

    /**
     * 根据 bizId 锁定
     *
     * @param bizId bizId
     * @return 非空，已经存在；空：不存在
     */
    default ENTITY lockByBizId(String bizId) {
        checkEntityAs(BizEntity.class);
        return getBaseMapper().selectForUpdateById((bizId));
    }


    /**
     * 分页查询
     *
     * @param pageQueryCondition 查询条件
     * @return 查询结果
     */
    default PageResult<ENTITY> page(BasePageQuery<ENTITY> pageQueryCondition) {
        handleBeforePageQuery(pageQueryCondition);
        Wrapper<ENTITY> wrapper = createPageQueryWrapper(pageQueryCondition);
        Page<ENTITY> page = convertToPage(pageQueryCondition, getEntityClass());
        // page 参数支持 Map /IPage ParameterUtils.findPage
        Page<ENTITY> pageResult = page(page, wrapper);
        return handlePageQueryResult(pageResult);
    }


    /**
     * 处理参数
     *
     * @param pageQueryCondition 分页参数
     */
    default void handleBeforePageQuery(BasePageQuery<ENTITY> pageQueryCondition) {
        // 驼峰 转 下划线 、过滤 SQL 关键字、防止注入等
    }


    /**
     * 处理时间区间，可以覆盖后处理组装查询条件
     *
     * @param pageQuery 分页查询条件
     * @return 查询构造器
     */
    default Wrapper<ENTITY> createPageQueryWrapper(BasePageQuery<ENTITY> pageQuery) {
        return query(pageQuery.getCondition(), pageQuery.getExt());
    }

    /**
     * 自定义处理返回结果
     *
     * @param page 分页对象
     * @return DTO
     */
    default PageResult<ENTITY> handlePageQueryResult(IPage<ENTITY> page) {
        return PageResult.IPageConverter.toResult(page);
    }

    // ----------------------------------------------------------------

    /**
     * 列出符合条件的数据
     *
     * @param example example
     * @return 符合条件的数据
     */
    default List<ENTITY> list(ENTITY example) {
        return list(example, null);
    }


    /**
     * 列出符合条件的数据
     *
     * @param example example
     * @param ext     扩展条件
     * @return 符合条件的数据
     */
    default List<ENTITY> list(ENTITY example, Map<String, Object> ext) {
        return getBaseMapper().selectList(query(example, ext));
    }

    /* ======================================= 工具类 ======================================= */

    /**
     * 查询，可重载，筛选列名
     *
     * @param entity 实体
     * @param ext    扩展条件
     * @return 结果
     */
    default Wrapper<ENTITY> query(ENTITY entity, Map<String, Object> ext) {
        if (entity == null) {
            // todo log
        }
        // 后面还可以加字符串，表示需要输出的列名
        QueryWrapper<ENTITY> wrapper = new QueryWrapper<>(entity);
        /*Wrapper<ENTITY> wrapper = query()
                .setEntity(entity)
                .setEntityClass(getEntityClass());*/
        return query(wrapper, ext, getEntityClass());
    }

    /**
     * 组装查询条件
     * 主要是从扩展条件中组装时间相关条件
     *
     * @param wrapper     wrapper
     * @param ext         扩展查询条件
     * @param entityClass 实体类型
     * @param <Entity>    泛型
     * @return wrapper
     */
    @SuppressWarnings("unchecked, rawtypes")
    static <Entity> Wrapper<Entity> query(Wrapper<Entity> wrapper, Map<String, Object> ext, Class<Entity> entityClass) {
        if (MapUtils.isEmpty(ext)) {
            return wrapper;
        }

        for (Map.Entry<String, Object> field : ext.entrySet()) {
            String key = field.getKey();
            Object value = field.getValue();
            if (ObjectUtil.isEmpty(value)) {
                continue;
            }
            // string to Date

            if (key.endsWith("_st")) {
                String beanField = StrUtil.subBefore(key, "_st", true);
                if (wrapper instanceof AbstractWrapper) {
                    ((AbstractWrapper) wrapper).ge(calculateDbField(beanField, entityClass), DateConverter.INSTANCE.convert(value.toString()));
                } else if (wrapper instanceof AbstractChainWrapper) {
                    ((AbstractChainWrapper) wrapper).ge(calculateDbField(beanField, entityClass), DateConverter.INSTANCE.convert(value.toString()));
                }
            }
            if (key.endsWith("_ed")) {
                String beanField = StrUtil.subBefore(key, "_ed", true);
                if (wrapper instanceof AbstractWrapper) {
                    ((AbstractWrapper) wrapper).le(calculateDbField(beanField, entityClass), DateConverter.INSTANCE.convert(value.toString()));
                } else if (wrapper instanceof AbstractChainWrapper) {
                    ((AbstractChainWrapper) wrapper).le(calculateDbField(beanField, entityClass), DateConverter.INSTANCE.convert(value.toString()));
                }
            }
        }
        return wrapper;
    }


    /**
     * 转为 pageDTO
     *
     * @param pageQuery   查询条件
     * @param entityClass 实体类
     * @param <Entity>    泛型
     * @return mybatis plus 的 Page
     */
    static <Entity> Page<Entity> convertToPage(BasePageQuery<Entity> pageQuery, Class<Entity> entityClass) {
        Page<Entity> page = new Page<Entity>(pageQuery.getPageNo(), pageQuery.getPageSize());
        if (CollectionUtils.isEmpty(pageQuery.getOrderRules())) {
            // 无排序参数
            return page;
        }

        List<OrderItem> orders = pageQuery.getOrderRules().stream()
                .map(r -> r.getOrder() == BasePageQuery.Order.ASC ?
                        OrderItem.asc(calculateDbField(r.getFieldName(), entityClass)) :
                        OrderItem.desc(calculateDbField(r.getFieldName(), entityClass))
                )
                .collect(Collectors.toList());

        page.setOrders(orders);
        return page;

    }

    /**
     * 根据 entity 字段 反射出 数据库字段
     *
     * @param fieldName 字段名
     * @param clazz     实体类型
     * @return 数据库字段名
     */
    static String calculateDbField(String fieldName, Class<?> clazz) {
        Field field = ReflectUtil.getField(clazz, fieldName);
        if (field == null) {
            return StrUtil.EMPTY;
        }
        TableField tf = field.getAnnotation(TableField.class);
        if (tf != null && StrUtil.isNotEmpty(tf.value())) {
            return tf.value();
        }
        return StrUtil.EMPTY;
    }

    // ================= extends =============

    /**
     * 根据 bizId 删除
     *
     * @param entity entity
     */
    default boolean removeByBizId(ENTITY entity) {
        checkEntityAs(BizEntity.class);
        return SqlHelper.retBool(getBaseMapper().deleteInLogicByBizId(entity));
    }

    /**
     * 删除（根据bizId 批量删除）
     *
     * @param entities entities 列表
     */
    default boolean removeByBizIds(Collection<ENTITY> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return false;
        }
        checkEntityAs(BizEntity.class);
        return SqlHelper.retBool(getBaseMapper().deleteInLogicByBizIdList(entities));
    }

    /**
     * 根据 bizId 选择修改
     *
     * @param entity 实体对象
     */
    default boolean updateByBizId(ENTITY entity) {
        checkEntityAs(BizEntity.class);
        return SqlHelper.retBool(getBaseMapper().updateByBizId(entity));
    }

    /**
     * 根据bizId 批量更新
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchByBizId(Collection<? extends ENTITY> entityList) {
        checkEntityAs(BizEntity.class);
        return updateBatchByBizId(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 根据bizId 批量更新
     *
     * @param entityList 实体对象集合
     * @param batchSize  更新批次数量
     */
    @SuppressWarnings("unchecked")
    default boolean updateBatchByBizId(Collection<? extends ENTITY> entityList, int batchSize) {
        return updateBatchById((Collection<ENTITY>) entityList, batchSize);
    }

    /**
     * TableId 注解存在更新记录，否插入一条记录
     *
     * @param entity 实体对象
     * @implSpec 根据 bizId 判断
     */
    @Override
    boolean saveOrUpdate(ENTITY entity);

    /**
     * 根据 bizId 查询
     *
     * @param bizId bizId
     */
    default ENTITY getByBizId(String bizId) {
        checkEntityAs(BizEntity.class);
        return getBaseMapper().selectByBizId(bizId);
    }

    /**
     * 查询（根据bizId 批量查询）
     *
     * @param bizIdList bizId列表
     */
    default List<ENTITY> listByBizIds(Collection<String> bizIdList) {
        checkEntityAs(BizEntity.class);
        return getBaseMapper().selectBatchBizIds(bizIdList);
    }

    /**
     * 检查是否可以转为 exceptedClass
     */
    default void checkEntityAs(Class<?> exceptedClass) {
        if (!exceptedClass.isAssignableFrom(getEntityClass())) {
            throw new IllegalStateException("not support such entity(" + getEntityClass().getName() +
                    ") for not extends " + exceptedClass.getName());
        }
    }

}
