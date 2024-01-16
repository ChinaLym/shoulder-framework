package org.shoulder.core.converter;

import org.springframework.core.convert.support.ConfigurableConversionService;

import java.util.Collection;
import java.util.List;

/**
 * Shoulder ConversionService
 * 比 spring 多了转换集合接口
 *
 * @author lym
 */
public interface ShoulderConversionService extends ConfigurableConversionService {

    /**
     * 数据转换 (Spring 方法)
     *
     * @param source 源数据
     * @param targetType 结果类型
     * @return T
     */
    //@Override
    //<T> T convert(Object source, @NonNull Class<T> targetType);

    /**
     * 集合类型数据转换 （本接口新增的）
     *
     * @param source     源数据
     * @param targetType 结果类型
     * @return List<T>
     */
    <S, T> List<T> convert(Collection<? extends S> source, Class<T> targetType);

}
