package org.shoulder.log.operation.model.sample;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.model.Operable;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.model.OperationDetailAble;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 被操作对象 DTO
 *
 * @author lym
 */
public class OperableObject implements Operable, OperationDetailAble {

    protected String objectId;

    protected String objectName;

    protected String objectType;

    protected List<String> detailItems;

    protected String detailKey;

    protected String detail;

    public OperableObject() {
    }

    public OperableObject(String id) {
        this.objectId = id;
    }

    public OperableObject(Operable operable) {
        this.objectId = operable.getObjectId();
        this.objectName = operable.getObjectName();
        this.objectType = operable.getObjectType();
        if (operable instanceof OperationDetailAble operationDetailAble) {
            if (CollectionUtils.isNotEmpty(operationDetailAble.getDetailItems())) {
                this.detailItems = operationDetailAble.getDetailItems();
            }
            if (StringUtils.isNotEmpty(operationDetailAble.getDetailKey())) {
                this.detailKey = operationDetailAble.getDetailKey();
            }
            if (StringUtils.isNotEmpty(operationDetailAble.getDetail())) {
                this.detail = operationDetailAble.getDetail();
            }
        }
    }

    public static List<OperableObject> of(Collection<? extends Operable> operables) {
        if (operables == null || operables.isEmpty()) {
            return Collections.emptyList();
        }
        return operables.stream().map(OperableObject::new).collect(Collectors.toList());
    }

    public static List<OperableObject> ofIds(Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().map(String::valueOf).map(OperableObject::new).collect(Collectors.toList());
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    public OperableObject setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    public OperableObject setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    public OperableObject setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    @Override
    public List<String> getDetailItems() {
        return detailItems;
    }

    public OperableObject setDetailItems(List<String> detailItems) {
        this.detailItems = detailItems;
        return this;
    }

    @Override
    public String getDetail() {
        return detail;
    }

    @Override
    public String getDetailKey() {
        return detailKey;
    }

    public void setDetailKey(String detailKey) {
        this.detailKey = detailKey;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public OperableObject addDetailItem(String... item) {
        if (this.detailItems == null) {
            this.detailItems = new LinkedList<>();
        }
        this.detailItems.addAll(Arrays.asList(item));
        return this;
    }

}
