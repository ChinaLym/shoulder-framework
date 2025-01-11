package org.shoulder.core.converter;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;

import java.util.Collection;
import java.util.List;

/**
 * Shoulder ConversionService
 * 比 spring 多了转换集合接口
 *
 * Spring 内使用的 ConversionService：
 * 1. 【非Bean】启动时会用 ApplicationConversionService 读配置文件；
 * 2. 【非Bean】ApplicationEnvironmentPreparedEvent EnvironmentPostProcessor 时，使用 ApplicationConversionService 读属性
 * 3. 【非Bean】background-preinit 线程去调用 SpringApplicationRunListener 适合会用
 * 4. 【Bean】Shoulder / Spring 定义的默认 ConversionService
 * 5. 【非Bean】Shoulder ConvertUtil 默认值
 * 6. 【Bean】Spring WEB 特定Bean，"mvcConversionService" {@link WebMvcAutoConfiguration.EnableWebMvcConfiguration#mvcConversionService()}
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

    void addConversionService(ConversionService conversionService);
}
