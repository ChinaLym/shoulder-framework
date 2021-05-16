package org.shoulder.log.operation.format;

import org.shoulder.log.operation.model.OperationLogDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * OperationLog param.value 转换器，用于定制日志参数格式
 * PS 自定义扩展时可使用枚举继承该接口
 *
 * @author lym
 */
public interface OperationLogParamValueConverter {


    /**
     * 转换日志参数值
     *
     * @param opLog            预先生成的日志实体，可以从这里拿到操动作标识等自定义业务标识
     * @param paramValue       参数值，paramValue.getClass() 不一定等于 methodParamClazz
     * @param methodParamClazz 代码中方法描述的参数类型
     * @return 转化后的 value，允许有多个值
     * @throws Exception 使用者在扩展时，可能不注意异常处理，会抛异常，因此框架需要捕获并给他警告
     */
    List<String> convert(@Nonnull OperationLogDTO opLog, @Nullable Object paramValue, Class methodParamClazz) throws Exception;

}
