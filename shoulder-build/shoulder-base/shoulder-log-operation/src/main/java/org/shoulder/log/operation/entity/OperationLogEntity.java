package org.shoulder.log.operation.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.constants.OperationResult;
import org.shoulder.log.operation.constants.TerminalType;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperateResult;
import org.shoulder.log.operation.dto.OperationDetailAble;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.normal.OpLogDetailKey;
import org.shoulder.log.operation.normal.Operation;
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
 * todo 修改校验；将原来 detail 拆成两个，生态接口需要补充定义。分为几个小对象
 *
 * @author lym
 */
@Data
@Accessors(chain = true)
public class OperationLogEntity implements Cloneable {

    /** ============= 操作者描述（操作者分两种情况，用户、服务内部任务） {@link Operator }  ================ */

    /**
     * 操作者标识 【必须】
     * 最长 128
     * 用户 ————————— 用户名（用户id或用户的登录账号），例： admin
     * 系统内部触发 —— 当前系统/服务标识   格式："system.服务标识" 例： system.user-center
     */
    protected String userId;

    /**
     * 操作者姓名（选填）
     * 最长 128
     * 用户 ————————— 用户昵称， 例： lym
     * 服务内部任务 —— 不填
     */
    protected String userName;

    /**
     * 操作者实名关联的人员唯一标识 255 （选填）
     * 用户 ————————— 用户对应的真实人标识 例： tb_person 表 主键
     * 服务内部任务 —— 不填
     */
    protected String personId;

    /**
     * 操作者组织唯一标识 128（选填）
     * 用户 ————————— 用户所属组织/部门唯一标识  例： tb_org 表的 主键
     * 服务内部任务 —— 不填
     */
    protected String userOrgId;

    /**
     * 操作者用户组名称 128 （选填）
     * 用户所属用户组名称
     */
    protected String userOrgName;

    /**
     * 操作者终端类型 （必填）
     */
    protected TerminalType terminalType = TerminalType.SYSTEM;

    /**
     * IP （选填）
     * 最长 255
     * 用户 ————————— 用户登录机器IP 例：10.16.66.66
     * 服务内部任务 —— 应用部署机器IP
     */
    protected String ip;

    /**
     * 操作者终端标识 128（选填），也可以生成 UUID
     * 手机设备的唯一标识：IMSI、IMEI、ESN、MEID
     * PC 中网卡唯一标识 MAC 地址 系统内部操作记为服务器所在机器 MAC 地址
     */
    protected String terminalId;

    /**
     * 操作者终端信息，如浏览器可获取 UserAgent、App 可获取手机类型 （选填）
     */
    protected String terminalInfo;

    // ====================================== 操作动作描述 ===================================

    /**
     * 具体操作标识      【必填】  相关接口：{@link Operation}
     * 最长 255   多语言翻译由服务提供。
     * 例：登录、退出、查询、新增、修改、删除、下载等
     */
    protected String operation;

    /**
     * 操作发生时间
     */
    protected LocalDateTime operationTime = LocalDateTime.now();

    /**
     * 操作参数，记录业务操作的参数信息，用于操作审计和分析 （选填），最长 4096
     *
     * @see OpLogParam
     * @see OperationLogParam
     */
    protected List<OpLogParam> params;

    /**
     * 本次操作的详细描述  最长 4096 （选填）
     * 详细的描述用户的操作内容，如被操作对象的 json 输出，注意不要输出密码等敏感信息，或是具体发生了怎样的事情
     */
    protected String detail;

    /**
     * 最长 128 （选填） 相关接口：{@link OpLogDetailKey}
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
     * 被操作对象的类型标识 最长 128 （选填）
     * Key格式为：log.objectType.<操作对象类型标识>
     */
    protected String objectType;

    /**
     * 被操作对象唯一标示 最长 128 （选填）
     * 例： tb_user 的 userId
     */
    protected String objectId;

    /**
     * 操作对象的名称  最长 255 （选填）
     * 若存在多个值时，以 ","分隔 例： "user1,user2,user3"
     */
    protected String objectName;

    // ========================= 其他描述 ======================

    /**
     * 与本次操作所关联其他业务操作的业务号 ，用于多个请求完共同完成一个业务功能时（选填）
     * 最长 128
     * 例   上传csv进行数据的批量导入场景：上传导入文件、校验导入数据、点击确认导入、导入成功业务相关可以填同一个标识符
     *      上传新头像、确定修改个人信息也可以有相同的业务标识
     */
    protected String businessId;

    /**
     * 服务标识 （必填） 默认取 `spring.application.name`
     */
    protected String serviceId;

    /**
     * 本次业务操作的业务号 最长 128（选填）
     */
    protected String traceId;

    /** 扩展字段 （选填） key 不能为 null */
    protected Map<String, String> extFields;

    // ***********************************  构造函数  ***********************************

    public OperationLogEntity() {
    }

    /**
     * 引导使用者填写必要字段
     */
    public OperationLogEntity(String operation) {
        this.operation = operation;
    }

    // ***********************************  增强的填充方式  ***********************************

    /**
     * 填充操作者信息
     */
    public OperationLogEntity setOperator(Operator operator) {
        if (operator != null) {
            this.userId = operator.getUserId();
            this.personId = operator.getPersonId();
            this.ip = operator.getIp();
            this.terminalId = operator.getTerminalId();
            this.terminalType = operator.getTerminalType();
        }
        return this;
    }

    /**
     * 填充单个被操作对象信息
     */
    public OperationLogEntity setOperableObject(Operable operable) {
        if (operable != null) {
            this.objectId = operable.getObjectId();
            this.objectName = operable.getObjectName();
            this.objectType = operable.getObjectType();

            if (operable instanceof OperationDetailAble && operable.getDetailItems() != null && operable.getDetailItems().size() > 0) {
                this.detailItems = operable.getDetailItems();
            }

            if (operable instanceof OperateResult) {
                this.setResult(OperationResult.of(((OperateResult) operable).success()));
            }
        }
        return this;
    }

    public OperationLogEntity addDetailItem(String detailItem) {
        if (detailItem != null) {
            this.detailItems.add(detailItem);
        }
        return this;
    }

    public OperationLogEntity setExtField(String extKey, String value) {
        if (extKey == null) {
            return this;
        }
        if(this.extFields == null){
            this.extFields = new LinkedHashMap<>();
        }
        this.extFields.put(extKey, value);
        return this;
    }

    public OperationLogEntity setResultFail() {
        return setResult(OperationResult.FAIL);
    }

    // ------------------ clone ---------------

    @Override
    public OperationLogEntity clone() {
        try {
            super.clone();
            return cloneTo(new OperationLogEntity());
        } catch (CloneNotSupportedException e) {
            throw new BaseRuntimeException(e);
        }
    }

    /**
     * 克隆日志实体
     */
    public OperationLogEntity cloneTo(@NonNull OperationLogEntity clone) {

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
        clone.setParams(params);//todo clone
        clone.setOperationTime(operationTime);
        clone.setResult(result);
        clone.setDetailKey(detailKey);
        clone.setDetailItems(new LinkedList<>(detailItems));
        clone.setDetail(detail);
        clone.setErrorCode(errorCode);

        clone.setServiceId(serviceId);
        clone.setTraceId(traceId);
        clone.setBusinessId(businessId);
        clone.setExtFields(extFields);

        return clone;
    }

}
