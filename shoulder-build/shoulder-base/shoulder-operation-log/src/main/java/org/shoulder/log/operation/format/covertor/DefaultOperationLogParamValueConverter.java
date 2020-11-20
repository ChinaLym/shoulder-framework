package org.shoulder.log.operation.format.covertor;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * 默认的 value 转换器
 * 对数组和可迭代的对象做了展平处理。
 * 普通对象如DTO 则使用其 toString() 方法，如 XxxDTO(field1=1, field2=2,field3=3)
 *
 * @author lym
 */
public class DefaultOperationLogParamValueConverter implements OperationLogParamValueConverter {

    /**
     * 当参数值为 null 时，输出内容
     */
    private String nullValueOutputText;

    public DefaultOperationLogParamValueConverter() {
        this("");
    }

    public DefaultOperationLogParamValueConverter(String nullValueOutputText) {
        this.nullValueOutputText = nullValueOutputText;
    }

    @Override
    public List<String> convert(@Nonnull OperationLogDTO opLog, @Nullable Object paramValue,
                                Class methodParamClazz) {
        return toStringList(paramValue);
    }

    protected List<String> toStringList(@Nullable Object obj) {

        List<String> resultList = new LinkedList<>();
        if (obj == null) {
            addNullValue(resultList);
            return resultList;
        }

        Class clazz = obj.getClass();
        if (obj instanceof Iterable) {
            // 可遍历的
            Iterable<?> iterableToLogParam = (Iterable<?>) obj;
            iterableToLogParam.forEach(
                item -> {
                    if (item != null) {
                        resultList.addAll(
                            toStringList(item)
                        );
                    } else {
                        addNullValue(resultList);
                    }
                });
        } else if (clazz.isArray()) {
            // 数组类型
            Object[] objects = ((Object[]) obj);
            for (Object object : objects) {
                if (object != null) {
                    resultList.addAll(toStringList(object));
                } else {
                    addNullValue(resultList);
                }
            }
        } else {
            // just to String（object类型、Map类型等）
            resultList.add(obj.toString());
        }
        return resultList;
    }

    private void addNullValue(List<String> resultList) {
        resultList.add(nullValueOutputText);
    }

}
