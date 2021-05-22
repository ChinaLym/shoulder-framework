package org.shoulder.log.operation.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 默认存储的
 *
 * @author lym
 */
public class HashMapOperableObjectTypeRepository implements OperableObjectTypeRepository {

    private static final ConcurrentMap<Class<?>, String> CACHE = new ConcurrentHashMap<>();

    private final Function<Class<?>, String> defaultTypeCalculator;

    public HashMapOperableObjectTypeRepository(Function<Class<?>, String> defaultTypeCalculator) {
        this.defaultTypeCalculator = defaultTypeCalculator;
    }

    @Override
    public String getObjectType(Class<?> operableClass) {
        return getObjectType(operableClass, defaultTypeCalculator);
    }

    @Override
    public String getObjectType(Class<?> operableClass, Function<Class<?>, String> defaultTypeCalculator) {
        return CACHE.computeIfAbsent(operableClass, defaultTypeCalculator);
    }

}
