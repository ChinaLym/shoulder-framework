package org.shoulder.validate.util;

import cn.hutool.core.util.ArrayUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 接口参数校验类
 * 附带常用的校验方法，若不满足条件，则抛出带错误码和支持多语言翻译的参数非法异常，并暗示框架通常返回 400 Http 状态，记录 info 日志
 * <p>
 * 格式不正确（长度、范围、正则等）统一返回格式不正确，人性化提示由前端提示
 *
 * @author lym
 */
public class ParamCheck {

    // ------------------------ 传入参数为空 ParamErrorCodeEnum.PARAM_BLANK -------------------

    /**
     * 非 null
     *
     * @param param     被校验的参数
     * @param paramName 参数名
     * @throws BaseRuntimeException 参数为空
     */
    public static void notNull(Object param, Object... paramName) throws BaseRuntimeException {
        if (param == null) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
    }

    /**
     * 如果参数为空，则抛带错误码的异常，此方法不支持多语言
     *
     * @param coll      被校验的参数
     * @param paramName 参数名
     * @throws BaseRuntimeException 参数为空
     */
    public static <T> void notEmpty(Collection<T> coll, Object... paramName) throws BaseRuntimeException {
        if (CollectionUtils.isEmpty(coll)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
    }

    /**
     * array 不为 null 且至少包含一个元素
     *
     * @param array     被检查的数组
     * @param paramName 参数名
     * @return 被检查的数组
     * @throws BaseRuntimeException array == null || array.length == 0
     */
    public static Object[] notEmpty(Object[] array, Object... paramName) throws BaseRuntimeException {
        if (array == null || array.length == 0) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return array;
    }

    /**
     * array 不包含 null
     *
     * @param <T>       数组类型
     * @param array     待检查的数组
     * @param paramName 参数名
     * @return array
     * @throws BaseRuntimeException array 中存在 null
     */
    public static <T> T[] noNullElements(T[] array, Object... paramName) throws BaseRuntimeException {
        if (ArrayUtil.hasNull(array)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return array;
    }

    /**
     * coll 不包含 null
     *
     * @param <T>       集合类型
     * @param coll      待检查的集合
     * @param paramName 参数名
     * @return coll
     * @throws BaseRuntimeException coll 中存在 null
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> noNullElements(Collection<T> coll, Object... paramName) throws BaseRuntimeException {
        if (CollectionUtils.containsAny(coll, (T) null)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return coll;
    }


    /**
     * map 不为空
     *
     * @param <K>       Key类型
     * @param <V>       Value类型
     * @param map       被检查的Map
     * @param paramName 参数名
     * @return map
     * @throws BaseRuntimeException MapUtils.isEmpty(map)
     */
    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, Object... paramName) throws BaseRuntimeException {
        if (MapUtils.isEmpty(map)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return map;
    }


    /**
     * param 非空
     *
     * @param <T>       字符串类型
     * @param param     被检查字符串
     * @param paramName 参数名
     * @return param
     */
    public static <T extends CharSequence> T notEmpty(T param, Object... paramName) throws BaseRuntimeException {
        if (StringUtils.isEmpty(param)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return param;
    }

    /**
     * param 非空串
     *
     * @param <T>       字符串类型
     * @param param     被检查字符串
     * @param paramName 参数名
     * @return param
     */
    public static <T extends CharSequence> T notBlank(T param, Object... paramName) throws BaseRuntimeException {
        if (StringUtils.isBlank(param)) {
            throw ParamErrorCodeEnum.PARAM_BLANK.toException(paramName);
        }
        return param;
    }

    /**
     * textToCheck 必须包含 mustContain 子串
     *
     * @param textToCheck   待检查的
     * @param mustContain   必须要包含的
     * @param invalidReason 非法原因
     * @return 被检查的子串
     * @throws BaseRuntimeException textToCheck.contains(mustContain) == true
     */
    public static String notContain(String textToCheck, String mustContain, Object invalidReason) throws BaseRuntimeException {
        if (StringUtils.isNotEmpty(textToCheck) &&
            StringUtils.isNotEmpty(mustContain) &&
            textToCheck.contains(mustContain)) {
            throw ParamErrorCodeEnum.PARAM_ILLEGAL.toException(invalidReason);
        }
        return mustContain;
    }

    // ------------ 参数范围检查 ParamErrorCodeEnum.PARAM_OUT_RANGE -----------

    /**
     * 大于等于某个值
     *
     * @param param     值
     * @param min       最小
     * @param paramName 参数名
     * @param <T>       泛型 Comparable
     * @return param
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> T eGreater(T param, T min, Object... paramName) throws BaseRuntimeException {
        if (param == null || param.compareTo(min) >= 0) {
            return param;
        }
        throw ParamErrorCodeEnum.PARAM_OUT_RANGE.toException(paramName);
    }

    /**
     * 大于某个值
     *
     * @param param     值
     * @param min       最小
     * @param paramName 参数名
     * @param <T>       泛型 Comparable
     * @return param
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> T greater(T param, T min, Object... paramName) throws BaseRuntimeException {
        if (param != null && param.compareTo(min) <= 0) {
            throw ParamErrorCodeEnum.PARAM_OUT_RANGE.toException(paramName);
        }
        return param;
    }

    /**
     * 在指定范围内
     *
     * @param param     值
     * @param min       最小值（包含）
     * @param max       最大值（包含）
     * @param paramName 参数名
     * @param <T>       Comparable
     * @return 通过后返回 param
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> T assertBetween(T param, T min, T max, Object... paramName) throws BaseRuntimeException {
        if (param != null && (min.compareTo(param) > 0 || max.compareTo(param) < 0)) {
            throw ParamErrorCodeEnum.PARAM_OUT_RANGE.toException(paramName);
        }
        return param;
    }


    /**
     * 检查分页查询参数是否符合要求
     *
     * @param pageSize 分页大小
     * @param pageNo   页码
     */
    public static void pageParam(Integer pageSize, Integer pageNo) {
        notNull(pageSize, "pageSize");
        notNull(pageNo, "pageNo");
        assertBetween(pageSize, 1, 1000, "pageSize");
        eGreater(pageNo, 1, "pageNo");
    }


    // ------------------------ 参数非法 ParamErrorCodeEnum.PARAM_INVALID -------------------

    /**
     * 断言对象是否为{@code null}
     *
     * @param object    被检查的对象
     * @param paramName 参数名
     * @throws BaseRuntimeException 参数为空
     */
    public static void isNull(Object object, Object... paramName) throws BaseRuntimeException {
        if (object != null) {
            throw ParamErrorCodeEnum.PARAM_ILLEGAL.toException(paramName);
        }
    }

    /**
     * 校验数据个数是否超过限制
     *
     * @param size      数据个数
     * @param limit     最大限制
     * @param paramName 参数名
     */
    public static void sizeLimit(int size, int limit, Object... paramName) throws BaseRuntimeException {
        if (size > 0 && size <= limit) {
            return;
        }
        throw ParamErrorCodeEnum.PARAM_ILLEGAL.toException(paramName);
    }


    /**
     * 校验集合元素是否超过限制个数
     *
     * @param paramName 参数名
     */
    public static <T> void sizeLimit(Collection<T> coll, int limit, Object... paramName) throws BaseRuntimeException {
        if (coll != null) {
            sizeLimit(coll.size(), limit, paramName);
        }
    }

    /**
     * 校验参数param在集合limits指定的元素范围中
     *
     * @param param     待校验的参数
     * @param allowData 允许的值
     * @param paramName 参数名
     */
    public static <T> T assertIn(T param, Collection<? extends T> allowData, Object... paramName) throws BaseRuntimeException {
        if (CollectionUtils.isEmpty(allowData) || !allowData.contains(param)) {
            throw ParamErrorCodeEnum.PARAM_ILLEGAL.toException(paramName);
        }
        return param;
    }


    // ------------ 参数过长 ParamErrorCodeEnum.PARAM_CONTENT_TOO_LONG -----------


    // ------------ 参数格式检查（正则） ParamErrorCodeEnum.PARAM_FORMAT_INVALID -----------


    // ------------ 参数类型检查 ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH -----------


    /**
     * 校验updateTime是否晚于或等于createTime
     *
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param paramName  参数名
     */
    public static void dateLater(Date createTime, Date updateTime, Object... paramName) throws BaseRuntimeException {
        if (createTime != null && updateTime != null && updateTime.before(createTime)) {
            throw ParamErrorCodeEnum.PARAM_ILLEGAL.toException(paramName);
        }
    }


    /**
     * obj 必须为 clazz 类型的实例
     *
     * @param <T>       被检查对象泛型类型
     * @param clazz     被检查对象匹配的类型
     * @param obj       被检查对象
     * @param paramName 参数名
     * @return 被检查对象
     * @throws BaseRuntimeException obj instanceof clazz == false
     * @see Class#isInstance
     */
    public static <T> T isInstanceOf(Class<?> clazz, T obj, Object... paramName) throws BaseRuntimeException {
        notNull(clazz);
        if (!clazz.isInstance(obj)) {
            throw ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH.toException(paramName);
        }
        return obj;
    }

    /**
     * superType 是 subType 的父类，可以接收 subType 类型
     *
     * @param superType 待检测的
     * @param subType   子类
     * @param paramName 参数名
     * @throws BaseRuntimeException superType 不是 subType 的父类
     * @see Class#isAssignableFrom
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, Object... paramName) throws BaseRuntimeException {
        notNull(superType, paramName);
        if (!superType.isAssignableFrom(subType)) {
            throw ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH.toException(paramName);
        }
    }

}
