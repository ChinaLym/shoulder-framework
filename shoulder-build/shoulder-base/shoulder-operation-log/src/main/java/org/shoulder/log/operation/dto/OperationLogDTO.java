package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.enums.OperationResult;
import org.shoulder.log.operation.enums.TerminalType;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 操作日志实体
 * （所有字段均为 protect: 方便使用者灵活扩展）
 * 重要信息：操作时间、用户id、用户名称，IP、操作动作、被操作对象、操作结果、操作详情
 * 其他信息：操作者终端标识、操作者使用的终端类型、追踪链路标识 traceId
 * todo 将原来 detail 拆成两个，生态接口需要补充定义。
 *
 * @author lym
 */
@Data
@Accessors(chain = true)
public class OperationLogDTO implements Cloneable {

    /** ============= 操作者描述（操作者分两种情况，用户、系统内部触发） {@link Operator }  ================ */

    /**
     * 操作者标识 【必须】
     * 用户 ————————— 用户名唯一标识，用户名、用户id等
     * 系统内部触发 —— 应用标识   格式为 "system.应用标识" 例： system.order
     */
    protected String userId;

    /**
     * 操作者姓名（选填）
     * 用户 ————————— 用户昵称， 例： lym
     * 系统内部触发 —— 不填
     */
    protected String userName;

    /**
     * 操作者实名关联的人员唯一标识 （选填）
     * 用户 ————————— 用户对应的真实人标识 例： tb_person 表 主键
     * 系统内部触发 —— 不填
     */
    protected String personId;

    /**
     * 操作者所属组唯一标识（选填）
     * 用户 ————————— 所属组织/用户组/群/部门标识  例： tb_user_group 表的 主键
     * 系统内部触发 —— 不填
     */
    protected String userOrgId;

    /**
     * 操作者所属组名称（选填）
     */
    protected String userOrgName;

    /**
     * 操作者终端类型 （必填）
     */
    protected TerminalType terminalType = TerminalType.SYSTEM;

    /**
     * IP （选填）
     * 用户终端机器IP 例：192.168.0.10
     * 若系统内部触发为服务部署机器IP
     */
    protected String ip;

    /**
     * 操作者终端标识 （选填），也可以生成 UUID
     * 手机设备的唯一标识：IMSI、IMEI、ESN、MEID
     * PC 中网卡唯一标识 MAC 地址 系统内部触发记为服务器所在机器 MAC 地址
     */
    protected String terminalId;

    /**
     * 操作者终端信息，如浏览器可获取 UserAgent、App 可获取手机类型 （选填）
     */
    protected String terminalInfo;

    // ====================================== 操作动作描述 ===================================

    /**
     * 具体操作标识 【必填】
     * 多语言翻译文件由服务提供，导入到网关或日志服务前端，展示时翻译。
     * 例：注册、登录、退出、查询、新增、修改、删除、下载等
     */
    protected String operation;

    /**
     * 操作发生时间
     */
    protected LocalDateTime operationTime = LocalDateTime.now();

    /**
     * 操作参数，记录业务操作的参数信息，用于操作审计和分析 （选填）
     *
     * @see OpLogParam
     * @see OperationLogParam
     */
    protected List<OpLogParam> params;

    /**
     * 本次操作的详细描述（选填）
     * 详细的描述用户的操作内容，如被操作对象的 json 输出，注意不要输出密码等敏感信息，或是具体发生了怎样的事情
     */
    protected String detail;

    /**
     * 操作详情对应的多语言key（选填）
     * - 若不支持多语言则不填。
     * 占位符使用 %1,%2,%n形式，n表示第n个参数；
     */
    protected String detailKey;

    /**
     * 操作详情占位填充项，用于填充 detailKey 中的 %1,%2,%n
     */
    protected List<String> detailItems;

    /**
     * 本次操作结果（必填）
     */
    protected OperationResult result = OperationResult.SUCCESS;


    /**
     * 当操作结果为失败时，记录操作失败对应的错误码（选填）
     */
    protected String errorCode;

    /** ================== 被操作对象描述（均为选填） {@link Operable } ================== */

    /**
     * 被操作对象的类型标识 （选填）
     * Key格式为：log.objectType.<操作对象类型标识>
     */
    protected String objectType;

    /**
     * 被操作对象唯一标示 （选填）
     * 例： tb_user 的 userId
     */
    protected String objectId;

    /**
     * 操作对象的名称 （选填）
     * 若存在多个值时，以 ","分隔 例： "user1,user2,user3"
     */
    protected String objectName;

    // ========================= 其他描述 ======================

    /**
     * 与本次操作所关联其他业务操作的业务号 ，用于多个请求完共同完成一个业务功能时（选填）
     * 例   上传csv进行数据的批量导入场景：上传导入文件、校验导入数据、点击确认导入、导入成功业务相关可以填同一个标识符
     *      上传新头像、确定修改个人信息也可以有相同的业务标识
     */
    protected String businessId;

    /**
     * 应用标识 （必填） 默认取 `spring.application.name`
     */
    protected String appId;

    /**
     * 本次业务操作的业务号（选填）
     */
    protected String traceId;

    /** 扩展字段 （选填） key 不能为 null */
    protected Map<String, String> extFields;

    // ***********************************  构造函数  ***********************************

    public OperationLogDTO() {
    }

    /**
     * 引导使用者填写必要字段
     */
    public OperationLogDTO(String operation) {
        this.operation = operation;
    }

    // ***********************************  增强的填充方式  ***********************************

    /**
     * 填充操作者信息
     */
    @JsonIgnore
    public OperationLogDTO setOperator(Operator operator) {
        if (operator != null) {
            this.userId = operator.getUserId();
            this.userName = operator.getUserName();
            this.personId = operator.getPersonId();
            this.userOrgId = operator.getUserOrgId();
            this.userOrgName = operator.getUserOrgName();
            this.ip = operator.getIp();
            this.terminalType = operator.getTerminalType();
            this.terminalId = operator.getTerminalId();
            this.terminalInfo = operator.getTerminalInfo();
        }
        return this;
    }

    /**
     * 填充单个被操作对象信息
     */
    @JsonIgnore
    public OperationLogDTO setOperableObject(Operable operable) {
        if (operable != null) {
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

            if (operable instanceof OperateResult) {
                this.setResult(OperationResult.of(((OperateResult) operable).success()));
            }
        }
        return this;
    }

    public OperationLogDTO addDetailItem(String detailItem) {
        if (detailItem != null) {
            this.detailItems.add(detailItem);
        }
        return this;
    }

    @JsonIgnore
    public OperationLogDTO setExtField(String extKey, String value) {
        if (extKey == null) {
            return this;
        }
        if(this.extFields == null){
            this.extFields = new LinkedHashMap<>();
        }
        this.extFields.put(extKey, value);
        return this;
    }

    @JsonIgnore
    public String getExtField(String extKey) {
        if (extKey == null || this.extFields == null) {
            return null;
        }
        return this.extFields.get(extKey);
    }

    @JsonIgnore
    public OperationLogDTO setResultFail() {
        return setResult(OperationResult.FAIL);
    }

    // ------------------ clone ---------------

    @Override
    public OperationLogDTO clone() {
        try {
            super.clone();
            return cloneTo(new OperationLogDTO());
        } catch (CloneNotSupportedException e) {
            throw new BaseRuntimeException(e);
        }
    }

    /**
     * 克隆日志实体
     */
    public OperationLogDTO cloneTo(@NonNull OperationLogDTO clone) {

        clone.setUserId(userId);
        clone.setUserName(userName);
        clone.setPersonId(personId);
        clone.setUserOrgId(userOrgId);
        clone.setTerminalType(terminalType);
        clone.setIp(ip);
        clone.setTerminalId(terminalId);
        clone.setTerminalInfo(terminalInfo);

        clone.setObjectId(objectId);
        clone.setObjectType(objectType);
        clone.setObjectName(objectName);

        clone.setOperation(operation);
        // 这里不是深克隆
        clone.setParams(params);
        clone.setOperationTime(operationTime);
        clone.setResult(result);
        clone.setDetailKey(detailKey);
        clone.setDetailItems(new LinkedList<>(detailItems));
        clone.setDetail(detail);
        clone.setErrorCode(errorCode);

        clone.setAppId(appId);
        clone.setTraceId(traceId);
        clone.setBusinessId(businessId);
        clone.setExtFields(extFields);

        return clone;
    }

}
