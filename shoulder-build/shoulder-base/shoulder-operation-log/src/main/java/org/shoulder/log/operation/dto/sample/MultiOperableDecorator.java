package org.shoulder.log.operation.dto.sample;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.log.operation.dto.Operable;

import java.util.Collection;

/**
 * 简单日志合并装饰器
 * 操作多个对象记录一条操作日志，一般用于 OperationLogInterceptorAdapter 中
 *
 * @author lym
 */
class MultiOperableDecorator extends OperableObject {

    /**
     * 省略符
     */
    private static final String ELLIPSIS = "...";

    public MultiOperableDecorator(Collection<? extends Operable> logObjects) {
        if (logObjects == null || logObjects.isEmpty()) {
            // 返回空对象
            return;
        }
        Operable logObject = logObjects.stream().findFirst().get();
        if (logObjects.size() == 1) {
            super.objectId = logObject.getObjectId();
            super.objectName = logObject.getObjectName();
            super.objectType = logObject.getObjectType();
        } else {

            StringBuilder objectsName = new StringBuilder();
            StringBuilder objectsId = new StringBuilder();
            for (Operable object : logObjects) {
                if (StringUtils.isNotEmpty(object.getObjectId())) {
                    objectsId.append(object.getObjectId()).append(",");
                }
                if (StringUtils.isNotEmpty(object.getObjectName())) {
                    objectsName.append(object.getObjectName()).append(",");
                }
            }
            if (StringUtils.isNotEmpty(objectsId)) {
                objectsId.deleteCharAt(objectsId.length() - 1);
            }
            if (StringUtils.isNotEmpty(objectsName)) {
                objectsName.deleteCharAt(objectsName.length() - 1);
            }

            // 日志规范不允许id或名称过长
            int idMaxLength = 127;
            int nameMaxLength = 255;
            if (objectsId.length() > idMaxLength) {
                objectsId.delete(idMaxLength - 3, objectsId.length()).append(ELLIPSIS);
            }
            if (objectsName.length() > nameMaxLength) {
                objectsName.delete(nameMaxLength - 3, objectsName.length()).append(ELLIPSIS);
            }
            super.objectId = objectsId.toString();
            super.objectName = objectsName.toString();
            super.objectType = logObject.getObjectType();
        }
    }

}
