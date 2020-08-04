package org.shoulder.data.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.data.annotation.TableAlias;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 自定义查询构造器
 *
 * @author lym
 */
public class CriteriaQueryWrapper<T> extends QueryWrapper<T> {

    private static final long serialVersionUID = 1L;
    /**
     * 外键表别名对象
     */
    protected Map<String, String> aliasMap = new HashMap<>();
    /**
     * 要查询字段 支持 tbName(或者 tableAliasName).fieldName
     */
    protected List<String> select = new LinkedList<>();
    /**
     * 分页依据
     */
    private PageQuery pageParams;


    public CriteriaQueryWrapper() {

    }

    public CriteriaQueryWrapper(PageQuery pageParams) {
        this.pageParams = pageParams;
        String sortField = pageParams.getSortBy();
        apply("1=1");

        // 待排序字段不为空时，添加排序
        if (ObjectUtils.isNotEmpty(sortField)) {
            String order = pageParams.getOrder();
            boolean isAsc = StringUtils.equalsIgnoreCase(SqlKeyword.ASC.name(), order) || StringUtils.isEmpty(order);
            sortField = StringUtils.camelToUnderline(sortField);
            orderBy(true, isAsc, sortField);
        }
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }


    /**
     * 创建外键表关联对象,需要在mapper(xml)中编写join
     */
    public void createAlias(String entry, String alias) {
        this.aliasMap.put(entry, alias);
    }


    /**
     * 创建外键表关联对象,需要在mapper(xml)中编写join
     */
    public void createAlias(Class cla) {
        TableAlias tableAlias = AnnotationUtils.getAnnotation(cla, TableAlias.class);
        if (ObjectUtils.isNotEmpty(tableAlias)) {
            this.aliasMap.put(tableAlias.value(), tableAlias.value());
        }
    }

    /**
     * 等于
     */
    @Override
    public CriteriaQueryWrapper<T> eq(String column, Object val) {
        super.eq(ObjectUtils.isNotEmpty(val) && !val.equals(-1) && !val.equals(-1L), column, val);
        return this;
    }

    /**
     * like
     */
    @Override
    public CriteriaQueryWrapper<T> like(String column, Object val) {
        like(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }

    /**
     * in
     */
    @Override
    public CriteriaQueryWrapper<T> in(String column, Object... val) {
        in(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }


    /**
     * ge
     */
    @Override
    public CriteriaQueryWrapper<T> ge(String column, Object val) {
        ge(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }

    /**
     * le
     */
    @Override
    public CriteriaQueryWrapper<T> le(String column, Object val) {
        le(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }

    /**
     * lt
     */
    @Override
    public CriteriaQueryWrapper<T> lt(String column, Object val) {
        lt(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }

    /**
     * gt
     */
    @Override
    public CriteriaQueryWrapper<T> gt(String column, Object val) {
        gt(ObjectUtils.isNotEmpty(val), column, val);
        return this;
    }


    /**
     * or
     */
    @Override
    public CriteriaQueryWrapper<T> or() {
        super.or();
        return this;
    }

    /**
     * likeLeft
     */
    @Override
    public QueryWrapper<T> likeLeft(String column, Object val) {
        return likeLeft(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * likeRight
     */
    @Override
    public QueryWrapper<T> likeRight(String column, Object val) {
        return likeRight(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 指定查询
     */
    public CriteriaQueryWrapper<T> select(String sql) {
        this.select.add(sql);
        return this;
    }

    public PageQuery getPagerInfo() {
        return pageParams;
    }

    public String getSelect() {
        StringBuffer str = new StringBuffer();
        String sqlSelect = getSqlSelect();
        if (ObjectUtils.isNotEmpty(sqlSelect)) {
            select.add(String.join(",", sqlSelect));
        }
        if (CollectionUtils.isEmpty(select)) {
            select.add("*");
        }
        return String.join(",", select);
    }


}
