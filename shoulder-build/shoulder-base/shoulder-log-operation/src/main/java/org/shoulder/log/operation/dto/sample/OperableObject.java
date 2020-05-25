package org.shoulder.log.operation.dto.sample;

import org.shoulder.log.operation.dto.ActionDetailAble;
import org.shoulder.log.operation.dto.Operable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 被操作对象 DTO
 * 
 * @author lym
 */
public class OperableObject implements Operable, ActionDetailAble {

    protected String objectId;

    protected String objectName;

    protected String objectType;

    protected List<String> actionDetail;

    public OperableObject(){}

    public OperableObject(Operable operable){
        this.objectId = operable.getObjectId();
        this.objectName = operable.getObjectName();
        this.objectType = operable.getObjectType();
        this.actionDetail = operable.getActionDetail();
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
    public List<String> getActionDetail() {
        return actionDetail;
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

    public OperableObject setActionDetail(List<String> actionDetail) {
        this.actionDetail = actionDetail;
        return this;
    }

    public OperableObject addActionDetailStr(String actionDetail){
        if (this.actionDetail == null){
            this.actionDetail = new LinkedList<>();
        }
        this.actionDetail.addAll(Arrays.asList(actionDetail.split(",")));
        return this;
    }

    public OperableObject addActionDetail(String actionDetail){
        if (this.actionDetail == null){
            this.actionDetail = new LinkedList<>();
        }
        this.actionDetail.add(actionDetail);
        return this;
    }

    public static List<OperableObject> of(Collection<? extends Operable> operables){
        if(operables == null || operables.isEmpty()){
            return Collections.emptyList();
        }
        return operables.stream().map(OperableObject::new).collect(Collectors.toList());
    }

}
