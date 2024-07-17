package org.shoulder.data.mybatis.interceptor.like;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.shoulder.core.util.StringUtils;

import java.util.*;

/**
 * mybatis/mybatis-plus模糊查询语句特殊字符转义拦截器
 *
 * @author lym
 */
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
@Slf4j
public class MybatisLikeSqlInterceptor implements Interceptor {

    /**
     * SQL语句like
     */
    private final static String SQL_LIKE = " like ";

    /**
     * SQL语句占位符
     */
    private final static String SQL_PLACEHOLDER = "?";

    /**
     * SQL语句占位符分隔
     */
    private final static String SQL_PLACEHOLDER_REGEX = "\\?";

    /**
     * 所有的转义器
     */
    private static final Map<Class<?>, AbstractLikeSqlConverter<?>> converterMap = new HashMap<>(4);

    static {
        converterMap.put(Map.class, new MapLikeSqlConverter());
        converterMap.put(Object.class, new ObjectLikeSqlConverter());
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        String sql = boundSql.getSql();
        this.transferLikeSql(sql, parameterObject, boundSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties arg0) {

    }

    /**
     * 修改包含like的SQL语句
     *
     * @param sql             SQL语句
     * @param parameterObject 参数对象
     * @param boundSql        绑定SQL对象
     */
    @SuppressWarnings("unchecked,rawtypes")
    private void transferLikeSql(String sql, Object parameterObject, BoundSql boundSql) {
        if (!isEscape(sql)) {
            return;
        }
        sql = sql.replaceAll(" {2}", " ");
        // 获取关键字的个数（去重）
        Set<String> fields = this.getKeyFields(sql, boundSql);
        if (fields == null) {
            return;
        }
        // 此处可以增强,不止是支持Map对象,Map对象仅用于传入的条件为Map或者使用@Param传入的对象被Mybatis转为的Map
        AbstractLikeSqlConverter converter;
        // 对关键字进行特殊字符“清洗”，如果有特殊字符的，在特殊字符前添加转义字符（\）
        if (parameterObject instanceof Map) {
            converter = converterMap.get(Map.class);
        } else {
            converter = converterMap.get(Object.class);
        }
        converter.convert(sql, fields, parameterObject);
    }

    /**
     * 是否需要转义
     *
     * @param sql SQL语句
     * @return true/false
     */
    private boolean isEscape(String sql) {
        return this.hasLike(sql) && this.hasPlaceholder(sql);
    }

    /**
     * 判断SQL语句中是否含有like关键字
     *
     * @param str SQL语句
     * @return true/false
     */
    private boolean hasLike(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.toLowerCase().contains(SQL_LIKE);
    }

    /**
     * 判断SQL语句中是否包含SQL占位符
     *
     * @param str SQL语句
     * @return true/false
     */
    private boolean hasPlaceholder(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.toLowerCase().contains(SQL_PLACEHOLDER);
    }

    /**
     * 获取需要替换的所有字段集合
     *
     * @param sql      完整SQL语句
     * @param boundSql 绑定的SQL对象
     * @return 字段集合列表
     */
    protected Set<String> getKeyFields(String sql, BoundSql boundSql) {
        String[] params = sql.split(SQL_PLACEHOLDER_REGEX);
        Set<String> fields = new HashSet<>();
        for (int i = 0; i < params.length; i++) {
            if (this.hasLike(params[i])) {
                String field = boundSql.getParameterMappings().get(i).getProperty();
                fields.add(field);
            }
        }
        return fields;
    }

}
