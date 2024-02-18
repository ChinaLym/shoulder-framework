package org.shoulder.log.operation.support;

import java.util.function.Function;

/**
 * 扩展：操作对象类型映射器
 * <p>
 * 用于可二次扩展的公共代码/框架中，已知操作对象class还要自动填充操作对象类型的场景，使用者可以外挂式获取操作对象类型
 * <p>
 * 如一个工程 X，依赖了A,B两模块( X -> A -> B)，X、A、B三个人开发
 * <ul>
 *  <li>B 模块作为框架，为通用对象（如 用户、设备、消息等各类模型）提供了的复杂逻辑的封装，使用 OperableObjectTypeRepository 获取操作对象类型
 *  <li>A 模块作为中间层，借用B为某个领域进行了封装（如 设备） ，单操作对象类型上是通用的，默认的对象类型是“设备”
 *  <li>X 作为产品级应用，希望给使用者展示具体的设备类型，如 摄像头、门禁卡、读卡器
 * </ul>
 * p>
 * 这个场景下，框架模块 B 提供 OperableObjectTypeRepository SPI，产品级 X 代码才能在不动 B 代码的情况下实现上述功能
 *
 * @author lym
 */
public interface OperableObjectTypeRepository {

    String getObjectType(Class<?> operableClass);

    String getObjectType(Class<?> operableClass, Function<Class<?>, String> defaultTypeCalculator);

}
