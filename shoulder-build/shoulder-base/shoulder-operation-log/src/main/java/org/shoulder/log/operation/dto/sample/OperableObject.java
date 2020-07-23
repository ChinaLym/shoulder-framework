package org.shoulder.log.operation.dto.sample;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationDetailAble;

import java.util.*;
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

    protected String detail;

    public OperableObject(){}

    public OperableObject(Operable operable){
        this.objectId = operable.getObjectId();
        this.objectName = operable.getObjectName();
        this.objectType = operable.getObjectType();
        if (operable instanceof OperationDetailAble) {
            OperationDetailAble operationDetailAble = (OperationDetailAble)operable;
            if(CollectionUtils.isNotEmpty(operationDetailAble.getDetailItems())){
                this.detailItems = operationDetailAble.getDetailItems();
            }
            if(StringUtils.isNotEmpty(operationDetailAble.getDetail())){
                this.detail = operationDetailAble.getDetail();
            }
        }
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public List<String> getDetailItems() {
        return detailItems;
    }

    @Override
    public String getDetail() {
        return detail;
    }

    public OperableObject setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public OperableObject setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    public OperableObject setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    public OperableObject setDetailItems(List<String> detailItems) {
        this.detailItems = detailItems;
        return this;
    }

    public OperableObject addDetailItem(String... item){
        if (this.detailItems == null){
            this.detailItems = new LinkedList<>();
        }
        this.detailItems.addAll(Arrays.asList(item));
        return this;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static List<OperableObject> of(Collection<? extends Operable> operables){
        if(operables == null || operables.isEmpty()){
            return Collections.emptyList();
        }
        return operables.stream().map(OperableObject::new).collect(Collectors.toList());
    }

}
