package org.shoulder.data.mybatis.template.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import org.shoulder.data.mybatis.template.entity.BaseEntity;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 扩展查询条件
 *
 * @author lym
 */
public class ExtendMap extends HashMap<String, Object> {

    @Serial private static final long serialVersionUID = 1L;

    public static final String MARK_IN = "_in";

    public static final String MARK_NOT_IN = "_notIn";

    public static final String MARK_NOT_EQUALS = "_ne";

    public static final String MARK_GE = "_ge";

    public static final String MARK_GT = "_gt";

    public static final String MARK_LE = "_le";

    public static final String MARK_LIKE_RIGHT = "_like_right";

    public static final String MARK_LIMIT = "_mark_limit";

    public static final String FOR_UPDATE = "_mark_ForUpdate";

    /**
     * 查询部分字段，value为具体字段名 List
     */
    public static final String SELECT_FIELDS = "_select_fields";

    /**
     * 查询部分字段,value为部分类型，由 repository 根据类型确定
     */
    public static final String SELECT_PARTIAL = "_select_partial";

    /**
     * 完全等于 value，使用 model
     *
     * @param dataObjFieldName fieldName
     * @param value value
     * @return this
     */
    //public ExtendMap eq(String dataObjFieldName, Object value) {
    //    put(dataObjFieldName, value);
    //    return this;
    //}

    /**
     * 不等于
     *
     * @param dataObjFieldName
     * @param value
     * @return
     */
    public ExtendMap ne(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_NOT_EQUALS, value);
        return this;
    }

    /**
     * in value，value is Collection
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap in(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_IN, value);
        return this;
    }

    /**
     * in value，value is Collection
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap notIn(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_NOT_IN, value);
        return this;
    }

    /**
     * 大于等于 value，时间起始
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap ge(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_GE, value);
        return this;
    }

    /**
     * 大于 value
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap gt(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_GT, value);
        return this;
    }

    /**
     * 小于等于 value，时间截止
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap le(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_LE, value);
        return this;
    }

    /**
     * left like
     *
     * @param dataObjFieldName fieldName
     * @param value            value
     * @return this
     */
    public ExtendMap likeRight(String dataObjFieldName, Object value) {
        put(dataObjFieldName + MARK_LIKE_RIGHT, value);
        return this;
    }

    /**
     * 查询部分字段
     *
     * @param columnName 列名
     * @return this
     */
    @SuppressWarnings("unchecked")
    public <DATA_OBJ extends BaseEntity> ExtendMap selectField(SFunction<DATA_OBJ, ?> columnName) {
        return selectField(Lists.newArrayList(columnName));
    }

    /**
     * 查询部分字段
     *
     * @param columnNames 列名
     * @return this
     */
    @SuppressWarnings("unchecked")
    public <DATA_OBJ extends BaseEntity> ExtendMap selectField(List<SFunction<DATA_OBJ, ?>> columnNames) {
        List<SFunction<DATA_OBJ, ?>> selectFields = (List<SFunction<DATA_OBJ, ?>>) computeIfAbsent(SELECT_FIELDS, k -> new LinkedList<>());
        selectFields.addAll(columnNames);
        return this;
    }

    /**
     * 查询部分字段
     *
     * @param briefMark 给 repository 看的标记
     * @return this
     */
    public ExtendMap selectPartial(Object briefMark) {
        put(SELECT_PARTIAL, briefMark);
        return this;
    }

    public ExtendMap limit(int limit) {
        put(MARK_LIMIT, limit);
        return this;
    }

    public ExtendMap forUpdate() {
        put(FOR_UPDATE, true);
        return this;
    }

}
