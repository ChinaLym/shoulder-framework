package org.shoulder.core.util;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.springframework.core.GenericTypeResolver;

import java.util.Arrays;
import java.util.List;

/**
 * 用于 shoulder 内部的工具，以避免耦合 optional 的依赖导致运行时意外的错误。
 *
 * @author lym
 */
public class ReflectionKit {

    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList("net.sf.cglib.proxy.Factory", "org.springframework.cglib.proxy.Factory", "javassist.util.proxy.ProxyObject", "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    /**
     * 获取父类定义的第 index 个泛型是什么
     *
     * @param clazz 目标类
     * @param genericIfc 父类
     * @param index 父类的泛型 index，从0开始
     * @return 具体类
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final Class<?> genericIfc, final int index) {
        Class<?> userClass = getUserClass(clazz);
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(userClass, genericIfc);
        return null == typeArguments ? null : typeArguments[index];
    }

    private static Class<?> getUserClass(Class<?> clazz) {
        AssertUtils.notNull(clazz, CommonErrorCodeEnum.CODING);
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();

            for (Class<?> cls : interfaces) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
