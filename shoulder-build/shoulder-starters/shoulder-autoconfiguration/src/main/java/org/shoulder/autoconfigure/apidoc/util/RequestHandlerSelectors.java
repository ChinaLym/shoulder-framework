package org.shoulder.autoconfigure.apidoc.util;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ClassUtils;
import springfox.documentation.RequestHandler;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * 在 {@link springfox.documentation.builders.RequestHandlerSelectors} 基础上扩展一些扫描判断方式
 *
 * @author lym
 */
public class RequestHandlerSelectors {

    // ---------------- 可以通过该方法自定义扫描接口的包路径正则 -------------

    /**
     * 包路径正则表达式匹配
     */
    public static Predicate<RequestHandler> packageRegex(String basePackage) {
        // 预编译一次，避免重复编译
        return packageRegex(Collections.singleton(basePackage));
    }

    /**
     * 包路径正则表达式匹配【支持多个】
     */
    public static Predicate<RequestHandler> packageRegex(Set<String> basePackagePatterns) {
        // 预编译一次，避免重复编译
        List<Pattern> patternList = CollectionUtils.emptyIfNull(basePackagePatterns).stream()
            .map(Pattern::compile)
            .collect(Collectors.toList());

        return input -> declaringClass(input).map(
            i -> {
                for (Pattern pattern : patternList) {
                    if (pattern.matcher(ClassUtils.getPackageName(i)).matches()) {
                        return true;
                    }
                }
                return false;
            }
        ).orElse(true);
    }

    // ---------------- 可以通过该方法自定义注解，约束要扫描哪些接口 -------------

    /**
     * 方法 / 类上有某个注解
     */
    public static Predicate<RequestHandler> withAnnotationOnClassOrMethod(Class<? extends Annotation> annotationClazz) {
        return input -> input.isAnnotatedWith(annotationClazz)
            || declaringClass(input).map(in -> in.isAnnotationPresent(annotationClazz)).orElse(false);
    }

    /**
     * 方法 / 类上有某个注解【支持多个】
     */
    public static Predicate<RequestHandler> withAnnotationOnClassOrMethod(@Nonnull Set<Class<? extends Annotation>> annotationClasses) {
        // annotationClasses 个数通常不会过多，故认为 两次循环开销 小于 反射获取方法所在类的开销
        return input -> {
            // 方法检查
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (input.isAnnotatedWith(annotationClass)) {
                    return true;
                }
            }
            // 类检查
            Class<?> declaringClass = declaringClass(input).orElse(null);
            if (declaringClass == null) {
                return false;
            }
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (annotationPresent(annotationClass).apply(annotationClass)) {
                    return true;
                }
            }
            return false;
        };
    }


    // -------------------------------------- 原有方法 --------------------------------------

    /**
     * Any RequestHandler satisfies this condition
     *
     * @return predicate that is always true
     */
    public static Predicate<RequestHandler> any() {
        return (each) -> true;
    }

    /**
     * No RequestHandler satisfies this condition
     *
     * @return predicate that is always false
     */
    public static Predicate<RequestHandler> none() {
        return (each) -> false;
    }

    /**
     * Predicate that matches RequestHandler with handlers methods annotated with given annotation
     *
     * @param annotation - annotation to check
     * @return this
     */
    public static Predicate<RequestHandler> withMethodAnnotation(final Class<? extends Annotation> annotation) {
        return input -> input.isAnnotatedWith(annotation);
    }

    /**
     * Predicate that matches RequestHandler with given annotation on the declaring class of the handler method
     *
     * @param annotation - annotation to check
     * @return this
     */
    public static Predicate<RequestHandler> withClassAnnotation(final Class<? extends Annotation> annotation) {
        return input -> declaringClass(input).map(annotationPresent(annotation)).orElse(false);
    }

    private static Function<Class<?>, Boolean> annotationPresent(final Class<? extends Annotation> annotation) {
        return input -> input.isAnnotationPresent(annotation);
    }


    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> ClassUtils.getPackageName(input).startsWith(basePackage);
    }

    /**
     * Predicate that matches RequestHandler with given base package name for the class of the handler method.
     * This predicate includes all request handlers matching the provided basePackage
     *
     * @param basePackage - base package of the classes
     * @return this
     */
    public static Predicate<RequestHandler> basePackage(String basePackage) {
        return input -> declaringClass(input).map(handlerPackage(basePackage)).orElse(true);
    }

    private static Optional<Class<?>> declaringClass(RequestHandler input) {
        return ofNullable(input.declaringClass());
    }

}
