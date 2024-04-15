package org.shoulder.core.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.ErrorCode;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 断言工具
 * <p>
 * 必须先调用 {@link #initByExceptionClass} 方法进行初始化（默认使用框架异常）
 * <li>给匿名bean注入属性exceptionClassName的值。
 * <li>exceptionClass必须是 {@link BaseRuntimeException} 的子类
 * 且实现构造方法: {@link BaseRuntimeException#BaseRuntimeException(String)}
 * {@link BaseRuntimeException#BaseRuntimeException(org.shoulder.core.exception.ErrorCode, Object[])}
 * </ul>
 * todo P1 add collection function
 *
 * @author lym
 */
@SuppressWarnings("rawtypes")
public class AssertUtils {

    /**
     * 异常构造方法
     */
    private static Constructor constructor;

    static {
        // 默认使用 shoulder 框架定义的
        initByExceptionClass(BaseRuntimeException.class);
    }

    // =========================== 初始化 ===========================

    /**
     * 通过异常类全限定名初始化AssertUtil
     *
     * @param exceptionClassName extends BaseRuntimeException
     */
    @SuppressWarnings("unchecked")
    public static void initByExceptionClassName(String exceptionClassName) {
        if (StringUtils.isBlank(exceptionClassName)) {
            throw new IllegalArgumentException("exceptionClassName can't be empty!");
        }
        Class<? extends BaseRuntimeException> exceptionClass;
        try {
            exceptionClass = (Class<? extends BaseRuntimeException>) Class.forName(exceptionClassName);
        } catch (Throwable e) {
            throw new IllegalArgumentException("loading exceptionClass FAILED![exceptionClassName="
                    + exceptionClassName + "]", e);
        }
        // 必须是AssertException的子类
        if (!BaseRuntimeException.class.isAssignableFrom(exceptionClass)) {
            throw new IllegalArgumentException(
                    "illegal exceptionClass type, must be the subclass of BaseRuntimeException![exceptionClass="
                            + exceptionClass + "]");
        }
        initByExceptionClass(exceptionClass);
    }

    /**
     * 通过异常类初始化AssertUtil配置。
     *
     * @param exceptionClass extends BaseRuntimeException
     */
    public static void initByExceptionClass(@Nonnull Class<? extends BaseRuntimeException> exceptionClass) {
        if (exceptionClass == null) {
            throw new IllegalArgumentException("exceptionClass can't be null!");
        }
        try {
            constructor = exceptionClass.getConstructor(ErrorCode.class, Object[].class);
        } catch (Throwable e) {
            throw new IllegalArgumentException("constructor method not found![exceptionClass="
                    + exceptionClass.getName() + "]", e);
        }
    }

    /**
     * 断言表达式的值为true，否则抛出指定错误信息。
     *
     * @param expValue  断言表达式
     * @param errorCode 错误码
     * @param args      任意个异常描述信息的参数
     */
    public static void isTrue(final boolean expValue, final ErrorCode errorCode, final Object... args) throws BaseRuntimeException {

        if (!expValue) {
            BaseRuntimeException exception;
            try {
                exception = (BaseRuntimeException) constructor.newInstance(errorCode, args);
            } catch (Throwable e) {
                throw new IllegalStateException(
                        "AssertUtils has not been initialized correctly![constructor="
                                + constructor + ",errorCode=" + errorCode + ",args="
                                + Arrays.toString(args) + "]", e);
            }
            throw exception;
        }
    }

    /**
     * 断言表达式的值为true，否则抛出指定错误信息。<br>
     * 增加断言回调处理，即使断言成功也会创建回调对象，可能会增加系统性能开销，慎用
     *
     * @param expValue  断言表达式
     * @param errorCode 错误码
     * @param callback  断言失败的回调方法
     */
    public static void isTrue(final boolean expValue, final ErrorCode errorCode,
                              Consumer<ErrorCode> callback) throws BaseRuntimeException {

        if (expValue) {
            return;
        }
        callback.accept(errorCode);
        isTrue(false, errorCode);
    }

    /**
     * 断言表达式的值为false，否则抛出指定错误信息。
     *
     * @param expValue  断言表达式
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void isFalse(final boolean expValue, final ErrorCode errorCode,
                               final Object... objs) throws BaseRuntimeException {
        isTrue(!expValue, errorCode, objs);
    }

    /**
     * 断言两个对象相等，否则抛出指定错误信息。
     *
     * @param obj1      待比较对象
     * @param obj2      待比较对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void equals(final Object obj1, final Object obj2, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(Objects.equals(obj1, obj2), errorCode, objs);
    }

    /**
     * 断言两个对象不等，否则抛出指定错误信息。
     *
     * @param obj1      待比较对象
     * @param obj2      待比较对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void notEquals(final Object obj1, final Object obj2, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(!Objects.equals(obj1, obj2), errorCode, objs);
    }

    /**
     * 断言两个对象相同，否则抛出指定错误信息。
     *
     * @param base      待比较对象
     * @param target    待比较对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void is(final Object base, final Object target, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(base == target, errorCode, objs);
    }

    /**
     * 断言两个对象不相同，否则抛出指定错误信息。
     *
     * @param base      待比较对象
     * @param target    待比较对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void isNot(final Object base, final Object target, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(base != target, errorCode, objs);
    }

    /**
     * 断言指定对象在容器中。否则抛出指定错误信息
     *
     * @param collection 容器集合
     * @param target     待查对象
     * @param errorCode  错误码
     * @param objs       任意个异常描述信息的参数
     */
    public static void contains(final Collection<?> collection, final Object target, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        notEmpty(collection, errorCode, objs);
        isTrue(CollectionUtils.containsAny(collection, target), errorCode, objs);
    }

    /**
     * 断言指定对象在容器中。否则抛出指定错误信息 (调用 equals)
     *
     * @param collection 容器集合
     * @param target     待查对象
     * @param errorCode  错误码
     * @param objs       任意个异常描述信息的参数
     */
    public static void contains(Object[] collection, Object target, ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(ArrayUtils.contains(collection, target), errorCode, objs);
    }

    /**
     * 断言指定对象不在容器中。否则抛出指定错误信息 (调用 equals)
     *
     * @param collection 容器集合
     * @param target     待查对象
     * @param errorCode  错误码
     * @param objs       任意个异常描述信息的参数
     */
    public static void notContains(final Object[] collection, final Object target, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(ArrayUtils.notContains(collection, target), errorCode, objs);
    }

    /**
     * 断言对象为空，否则抛出指定错误信息。
     *
     * @param str       断言字符串
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void blank(@Nullable final String str, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(StringUtils.isBlank(str), errorCode, objs);
    }

    /**
     * 断言对象为非空，否则抛出指定错误信息。
     *
     * @param str       断言字符串
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void notBlank(@Nonnull final String str, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(StringUtils.isNotBlank(str), errorCode, objs);
    }

    /**
     * 断言对象为空，否则抛出指定错误信息。
     *
     * @param str       断言字符串
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void empty(@Nullable final String str, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(StringUtils.isEmpty(str), errorCode, objs);
    }

    /**
     * 断言对象为非空，否则抛出指定错误信息。
     *
     * @param str       断言字符串
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void notEmpty(@Nonnull final String str, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(StringUtils.isNotEmpty(str), errorCode, objs);
    }

    /**
     * 断言对象为null，否则抛出指定错误信息。
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void isNull(@Nullable final Object object, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(object == null, errorCode, objs);
    }

    /**
     * 断言对象非null，否则抛出指定错误信息。
     *
     * @param object    待检查对象
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void notNull(@Nonnull final Object object, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(object != null, errorCode, objs);
    }

    /**
     * 断言集合不为空或null，否则抛出指定错误信息。
     *
     * @param collection 待检查集合
     * @param errorCode  错误码
     * @param objs       任意个异常描述信息的参数
     */
    public static void notEmpty(@Nonnull final Collection collection, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(!CollectionUtils.isEmpty(collection), errorCode, objs);
    }

    /**
     * 断言集合为空或null，否则抛出指定错误信息。
     *
     * @param collection 待检查集合
     * @param errorCode  错误码
     * @param objs       任意个异常描述信息的参数
     */
    public static void empty(@Nullable final Collection collection, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(CollectionUtils.isEmpty(collection), errorCode, objs);
    }

    /**
     * 断言map不为空或null，否则抛出指定错误信息。
     *
     * @param map       待检查map
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void notEmpty(@Nonnull final Map map, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(!MapUtils.isEmpty(map), errorCode, objs);
    }

    /**
     * 断言map为空或null，否则抛出指定错误信息。
     *
     * @param map       待检查map
     * @param errorCode 错误码
     * @param objs      任意个异常描述信息的参数
     */
    public static void empty(@Nullable final Map map, final ErrorCode errorCode, final Object... objs) throws BaseRuntimeException {
        isTrue(MapUtils.isEmpty(map), errorCode, objs);
    }

}
