package org.shoulder.data.mybatis.template.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.converter.DateConverter;
import org.shoulder.core.dto.request.BasePageQuery;
import org.shoulder.core.dto.response.PageResult;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * shoulder 定义的基本服务，提供了增删改查等通用代码
 *
 * @author lym
 */
public interface BaseService<ENTITY> extends IService<ENTITY> {

    /**
     * 根据id修改 entity 的所有字段，包含 NULL
     *
     * @param entity 实体
     * @return 更新成功
     */
    boolean updateAllById(ENTITY entity);

    /**
     * 根据主键锁定
     *
     * @param entity 获取到的行数据
     * @return 非空，已经存在；空：不存在
     */
    ENTITY lockById(ENTITY entity);

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

    default Wrapper<ENTITY> query(ENTITY entity, Map<String, Object> ext) {
        Wrapper<ENTITY> wrapper = query()
                .setEntity(entity)
                .setEntityClass(getEntityClass());
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

}
