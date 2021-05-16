package org.shoulder.log.operation.support;

import java.util.function.Function;

/**
 * @author lym
 */
public interface OperableObjectTypeRepository {

    String getObjectType(Class<?> operableClass);

    String getObjectType(Class<?> operableClass, Function<Class<?>, String> defaultTypeCalculator);

}
