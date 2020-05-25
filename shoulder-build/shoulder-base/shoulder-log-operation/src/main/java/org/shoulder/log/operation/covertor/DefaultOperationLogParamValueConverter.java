package org.shoulder.log.operation.covertor;

import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * 默认的 value 转换器
 *
 * 对可遍历的对象做了展平处理，如 List<String>、 xxxObject[]。如 1,2,3
 *
 * 普通对象如DTO 则使用其 toString() 方法，如 XxxDTO(field1=1, field2=2,field3=3)
 *
 * @author lym
 */
public class DefaultOperationLogParamValueConverter implements OperationLogParamValueConverter {

    /**
     * 当参数值为 null 时，输出内容
     */
    private String nullValueOutputText = "";

    public DefaultOperationLogParamValueConverter(String nullValueOutputText){
        this.nullValueOutputText = nullValueOutputText;
    }

    @Override
    public List<String> convert(@NonNull OperationLogEntity logEntity, @Nullable Object paramValue,
                                Class methodParamClazz) {
        return toStringList(paramValue);
    }

    protected List<String> toStringList(@Nullable Object obj){

        List<String> resultList = new LinkedList<>();
        if(obj == null){
            addNullValue(resultList);
            return resultList;
        }

        Class clazz = obj.getClass();
        if (obj instanceof Iterable) {
            Iterable<?> iterableToLogParam = (Iterable<?>) obj;
            iterableToLogParam.forEach(
                    item -> {
                        if (item != null) {
                            resultList.addAll(
                                    toStringList(item)
                            );
                        }else {
                            addNullValue(resultList);
                        }
                    });
        } else if (clazz.isArray()) {
            Object[] objects = ((Object[]) obj);
            for (Object object : objects) {
                if (object != null) {
                    resultList.addAll(toStringList(object));
                }else {
                    addNullValue(resultList);
                }
            }
        } else {
            // if Map...other object then just to String
            resultList.add(obj.toString());
        }
        return resultList;
    }

    private void addNullValue(List<String> resultList){
        resultList.add(nullValueOutputText);
    }

}