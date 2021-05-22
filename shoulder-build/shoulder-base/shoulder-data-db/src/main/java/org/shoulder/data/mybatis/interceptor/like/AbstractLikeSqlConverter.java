package org.shoulder.data.mybatis.interceptor.like;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 包含like的SQL语句转义模板
 *
 * @author lym
 */
@Slf4j
public abstract class AbstractLikeSqlConverter<T> {

    /**
     * SQL语句like使用关键字%
     */
    private final static String LIKE_SQL_KEY = "%";

    /**
     * SQL语句需要转义的关键字
     */
    private final static String[] ESCAPE_CHAR = new String[]{LIKE_SQL_KEY, "_", "\\"};

    /**
     * mybatis-plus中like的SQL语句样式
     */
    private final static String MYBATIS_PLUS_LIKE_SQL = " like ?";

    /**
     * mybatis-plus中参数前缀
     */
    private final static String MYBATIS_PLUS_WRAPPER_PREFIX = "ew.paramNameValuePairs.";

    /**
     * mybatis-plus中参数键
     */
    final static String MYBATIS_PLUS_WRAPPER_KEY = "ew";

    /**
     * mybatis-plus中参数分隔符
     */
    final static String MYBATIS_PLUS_WRAPPER_SEPARATOR = ".";

    /**
     * mybatis-plus中参数分隔符替换器
     */
    final static String MYBATIS_PLUS_WRAPPER_SEPARATOR_REGEX = "\\.";

    /**
     * 已经替换过的标记
     */
    final static String REPLACED_LIKE_KEYWORD_MARK = "replaced.keyword";

    /**
     * 转义特殊字符
     *
     * @param sql       SQL语句
     * @param fields    字段列表
     * @param parameter 参数对象
     */
    public void convert(String sql, Set<String> fields, T parameter) {
        for (String field : fields) {
            if (this.hasMybatisPlusLikeSql(sql)) {
                if (this.hasWrapper(field)) {
                    // 第一种情况：在业务层进行条件构造产生的模糊查询关键字,使用QueryWrapper,LambdaQueryWrapper
                    this.transferWrapper(field, parameter);
                } else {
                    // 第二种情况：未使用条件构造器，但是在service层进行了查询关键字与模糊查询符`%`手动拼接
                    this.transferSelf(field, parameter);
                }
            } else {
                // 第三种情况：在Mapper类的注解SQL中进行了模糊查询的拼接
                this.transferSplice(field, parameter);
            }
        }
    }

    /**
     * 转义条件构造的特殊字符
     * 在业务层进行条件构造产生的模糊查询关键字,使用QueryWrapper,LambdaQueryWrapper
     *
     * @param field     字段名称
     * @param parameter 参数对象
     */
    public abstract void transferWrapper(String field, T parameter);

    /**
     * 转义自定义条件拼接的特殊字符
     * 未使用条件构造器，但是在service层进行了查询关键字与模糊查询符`%`手动拼接
     *
     * @param field     字段名称
     * @param parameter 参数对象
     */
    public abstract void transferSelf(String field, T parameter);

    /**
     * 转义自定义条件拼接的特殊字符
     * 在Mapper类的注解SQL中进行了模糊查询的拼接
     *
     * @param field     字段名称
     * @param parameter 参数对象
     */
    public abstract void transferSplice(String field, T parameter);

    /**
     * 转义通配符
     *
     * @param before 待转义字符串
     * @return 转义后字符串
     */
    String escapeChar(String before) {
        if (StringUtils.isNotBlank(before)) {
            before = before.replaceAll("\\\\", "\\\\\\\\");
            before = before.replaceAll("_", "\\\\_");
            before = before.replaceAll("%", "\\\\%");
        }
        return before;
    }

    /**
     * 是否包含需要转义的字符
     *
     * @param obj 待判断的对象
     * @return true/false
     */
    boolean hasEscapeChar(Object obj) {
        if (!(obj instanceof String)) {
            return false;
        }
        return this.hasEscapeChar((String) obj);
    }

    /**
     * 处理对象like问题
     *
     * @param field     对象字段
     * @param parameter 对象
     */
    void resolveObj(String field, Object parameter) {
        if (parameter == null || StringUtils.isBlank(field)) {
            return;
        }
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field, parameter.getClass());
            Method readMethod = descriptor.getReadMethod();
            Object param = readMethod.invoke(parameter);
            if (this.hasEscapeChar(param)) {
                Method setMethod = descriptor.getWriteMethod();
                setMethod.invoke(parameter, this.escapeChar(param.toString()));
            } else if (this.cascade(field)) {
                int index = field.indexOf(MYBATIS_PLUS_WRAPPER_SEPARATOR) + 1;
                this.resolveObj(field.substring(index), param);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            log.error("反射 {} 的 {} get/set方法出现异常", parameter, field, e);
        }
    }

    /**
     * 判断是否是级联属性
     *
     * @param field 字段名
     * @return true/false
     */
    boolean cascade(String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }
        return field.contains(MYBATIS_PLUS_WRAPPER_SEPARATOR) && !this.hasWrapper(field);
    }

    /**
     * 是否包含mybatis-plus的包含like的SQL语句格式
     *
     * @param sql 完整SQL语句
     * @return true/false
     */
    private boolean hasMybatisPlusLikeSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }
        return sql.toLowerCase().contains(MYBATIS_PLUS_LIKE_SQL);
    }

    /**
     * 判断是否使用mybatis-plus条件构造器
     *
     * @param field 字段
     * @return true/false
     */
    private boolean hasWrapper(String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }
        return field.contains(MYBATIS_PLUS_WRAPPER_PREFIX);
    }

    /**
     * 判断字符串是否含有需要转义的字符
     *
     * @param str 待判断的字符串
     * @return true/false
     */
    private boolean hasEscapeChar(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        for (String s : ESCAPE_CHAR) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }

}
