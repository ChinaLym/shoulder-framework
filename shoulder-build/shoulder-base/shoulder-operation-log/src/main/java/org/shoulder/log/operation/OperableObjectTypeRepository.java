package org.shoulder.log.operation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author lym
 */
public interface OperableObjectTypeRepository {

    ConcurrentMap<Class<?>, String> CACHE = new ConcurrentHashMap<>();

    default String getObjectType(Class<?> operableClass) {
        return getObjectType(operableClass, c -> "objectType." + c.getSimpleName());
    }

    default String getObjectType(Class<?> operableClass, Function<Class<?>, String> defaultTypeCalculator) {
        String type = CACHE.get(operableClass);
        return type != null ? type : defaultTypeCalculator.apply(operableClass);
    }

}
