package org.shoulder.log.operation.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.constants.OpLogConstants;
import org.shoulder.log.operation.constants.OperateResultEnum;
import org.shoulder.log.operation.dto.ActionDetailAble;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperateResult;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.normal.Action;
import org.shoulder.log.operation.normal.DetailI18nKey;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;


/**
 * 操作日志实体
 * （所有字段均为 protect: 方便使用者灵活扩展）
 * 重要信息：操作时间、用户id、用户名称，IP、操作动作、被操作对象、操作结果、操作详情
 * 其他信息：操作者所在机器的MAC地址、操作者使用的终端类型、追踪链路标识 traceId
 *
 * todo 日志多语言实现设计和说明
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
     * 用户 ————————— 用户名（用户的账号），例： admin
     * 服务内部任务 —— 执行任务的服务   格式："service.服务标识" 例： service.user-center
     */
    protected String userId;

    /**
     * 操作者姓名（选填）
     * 最长 255
     * 用户 ————————— 用户昵称， 例： lym
     * 服务内部任务 —— 不填
     */
    protected String userName;

    /**
     * 操作者实名关联的人员唯一标识（选填）
     * 最长 255
     * 用户 ————————— 用户对应的真实人标识 例： tb_person 表 主键
     * 服务内部任务 —— 不填
     */
    protected String personId;

    /**
     * 操作者组织唯一标识（选填）
     * 最长 128
     * 用户 ————————— 用户所属组织/部门唯一标识  例： tb_org 表的 主键
     * 服务内部任务 —— 不填
     */
    protected String userOrgId;

    /**
     * IP （选填）
     * 最长 255
     * 用户 ————————— 用户登录机器IP 例：10.16.66.66
     * 服务内部任务 —— 应用部署机器IP
     */
    protected String ip;

    /**
     * MAC地址 （选填）
     * 最长 255
     * 用户 ————————— 用户登录机器MAC地址 例：8C-RC-5B-54-89-2P
     * 服务内部任务 —— 服务所在机器MAC地址
     */
    protected String mac;

    /**
     * 操作者终端类型 （必填）- 默认值 web
     */
    protected String terminalType = OpLogConstants.TerminalType.WEB;

    /** ================== 被操作对象描述（均为选填） {@link Operable } ================== */

    /**
     * 被操作对象的类型标识 最长 128 （选填）
     * Key格式为：log.objectType.<操作对象类型表示>
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

    // ====================================== 操作动作描述 ===================================

    /**
     * 具体操作标识      【必填】  相关接口：{@link Action}
     * 最长 255   多语言翻译由服务提供。
     * 例：登录、退出、查询、新增、修改、删除、下载等
     */
    protected String action;

    /**
     * 操作发生时间
     */
    protected LocalDateTime operationTime = LocalDateTime.now();

    /**
     * 本次操作结果       （必填） 默认成功
     */
    protected OperateResultEnum result = OperateResultEnum.SUCCESS;

    /**
     * 最长 128 （选填） 相关接口：{@link DetailI18nKey}
     * - 若不支持多语言则不填。
     * 占位符使用 %1,%2,%n形式，n表示第n个参数；
     */
    protected String detailI18nKey;

    /**
     * 操作详情占位填充项，用于填充 detailI18nKey 中的 %1,%2,%n
     */
    protected List<String> detailItem;

    /**
     * 本次操作的详细描述  最长 4096 （选填） todo 分为两个字段
     * 以上数据项无法清楚描述的情况下能通过本项尽可能准确、详细的描述用户的操作内容
     */
    protected String detail;

    // ========================= 其他描述（均为选填） ======================

    /**
     * 操作参数，记录业务操作的参数信息，用于操作审计和分析 （选填），最长 4096
     *
     * @see ActionParam
     * @see OperationLogParam
     */
    protected List<ActionParam> actionParams;

    /**
     * 服务标识 （必填） 默认为 `spring.application.name`
     */
    protected String serviceId;

    /**
     * 本次业务操作的业务号 最长 128（选填）
     */
    protected String traceId;

    /**
     * 与本次操作所关联其他业务操作的业务号 （选填）
     * 最长 128
     * 例1：结束视频预览，与开始视频预览的业务有关联，该字段可填写开始视频预览操作的业务号；
     * 例2：批量导入场景，确认导入时，与上传批量导入文件操作有关；
     */
    protected String relationId;

    /**
     * 当操作结果为失败时，记录操作失败对应的错误码（选填）
     */
    protected String errorCode;


    // ***********************************  构造函数  ***********************************

    public OperationLogEntity() {
    }

    /**
     * 引导使用者填写必要字段
     */
    public OperationLogEntity(String action) {
        super();
        this.action = action;
    }

    /**
     * @deprecated use {@link #clone} 推荐使用 .clone 方法来复制，可读性更好。
     */
    public OperationLogEntity(OperationLogEntity opLogEntity) {
        opLogEntity.cloneTo(this);
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
            this.mac = operator.getMac();
            this.terminalType = OpLogConstants.TerminalType.WEB;
        }
        return this;
    }

    /**
     * 填充被操作对象信息
     */
    public OperationLogEntity setOperableObject(Operable operable) {
        if (operable != null) {
            this.objectId = operable.getObjectId();
            this.objectName = operable.getObjectName();
            this.objectType = operable.getObjectType();

            if (operable instanceof ActionDetailAble && operable.getActionDetail() != null && operable.getActionDetail().size() > 0) {
                this.detailItem = operable.getActionDetail();
            }

            if (operable instanceof OperateResult) {
                this.setResult(OperateResultEnum.of(((OperateResult) operable).success()));
            }
        }
        return this;
    }

    public OperationLogEntity addDetailItem(String actionDetail) {
        if (actionDetail != null) {
            this.detailItem.add(actionDetail);
        }
        return this;
    }


    // **************************************  格式化 todo 放外部  ************************************

    /**
     * 日期格式化:线程安全高性能
     */
    private static FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * 按照规范格式拼接日志字符串。必填项不进行非空校验，规范要求的前后缀会根据属性值是否规范进行动态矫正
     * 只是单纯按照格式拼接，禁止在本方法动态修改实体属性值
     *
     * @return 规范要求的日志格式的 string
     */
    @Override
    public String toString() {
        StringBuilder logString = new StringBuilder();

        // action
        boolean hasActionIdPrefix = action.startsWith(OpLogConstants.I18nPrefix.ACTION);
        logString.append("action:\"");
        if (hasActionIdPrefix) {
            logString.append(this.action);
        } else {
            logString.append(OpLogConstants.I18nPrefix.ACTION)
                    .append(this.action);
        }
        logString.append("\",");

        // objectType
        if (StringUtils.isNotEmpty(this.objectType)) {
            boolean hasObjectTypePrefix = objectType.startsWith(OpLogConstants.I18nPrefix.OBJECT_TYPE);
            logString.append("objectType:\"");
            if (hasObjectTypePrefix) {
                logString.append(this.objectType);
            } else {
                logString.append(OpLogConstants.I18nPrefix.OBJECT_TYPE)
                        .append(this.objectType);
            }
            logString.append("\",");
        }

        // i18nKey
        if (StringUtils.isNotEmpty(this.detailI18nKey)) {
            logString.append("i18nKey:\"");
            boolean hasMsgIdIdPrefix = detailI18nKey.startsWith(OpLogConstants.I18nPrefix.ACTION_DETAIL);
            if (hasMsgIdIdPrefix) {
                logString.append(this.detailI18nKey);
            } else {
                logString.append(OpLogConstants.I18nPrefix.ACTION_DETAIL)
                        .append(this.detailI18nKey);
            }
            logString.append("\",");
        }

        if (CollectionUtils.isNotEmpty(this.detailItem)) {
            StringJoiner detailsStr = new StringJoiner(",");
            detailItem.stream().filter(Objects::nonNull).forEach(detailsStr::add);
            logString.append("actionDetail:\"")
                    .append(detailsStr.toString())
                    .append("\",");
        }

        if (StringUtils.isNotEmpty(this.ip)) {
            logString.append("ip:\"").append(this.ip).append("\",");
        }

        if (StringUtils.isNotEmpty(this.mac)) {
            logString.append("mac:\"").append(this.mac).append("\",");
        }

        if (StringUtils.isNotEmpty(this.objectId)) {
            logString.append("objectId:\"").append(this.objectId).append("\",");
        }

        if (StringUtils.isNotEmpty(this.objectName)) {
            logString.append("objectName:\"").append(this.objectName).append("\",");
        }

        logString.append("serviceId:\"").append(this.serviceId).append("\",");
        logString.append("terminalType:\"").append(this.terminalType).append("\",");

        logString.append("userId:\"").append(this.userId).append("\",");
        logString.append("result:\"").append(this.result.getCode()).append("\",");
        logString.append("operationTime:\"").append(fastDateFormat.format(this.operationTime)).append("\",");

        if (StringUtils.isNotEmpty(this.userName)) {
            logString.append("userName:\"").append(this.userName).append("\",");
        }

        if (StringUtils.isNotEmpty(this.userOrgId)) {
            logString.append("userOrgId:\"").append(this.userOrgId).append("\",");
        }

        if (StringUtils.isNotEmpty(this.getTraceId())) {
            logString.append("traceId:\"").append(this.getTraceId()).append("\",");
        }

        if (StringUtils.isNotEmpty(this.relationId)) {
            logString.append("relationId:\"").append(this.relationId).append("\",");
        }

        if (StringUtils.isNotEmpty(this.personId)) {
            logString.append("personId:\"").append(this.personId).append("\",");
        }

        if (CollectionUtils.isNotEmpty(actionParams)) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            actionParams.stream().map(param -> param.format(action)).filter(Objects::nonNull).forEach(sj::add);
            logString
                    .append("actionParam:\"")
                    .append(sj)
                    .append("\",");
        }


        logString.setLength(logString.length() - 1);
        return logString.toString();
    }

    @Override
    public OperationLogEntity clone() {
        return cloneTo(new OperationLogEntity());
    }

    /**
     * 克隆日志实体
     */
    private OperationLogEntity cloneTo(@NonNull OperationLogEntity clone) {

        clone.setUserId(userId);
        clone.setUserName(userName);
        clone.setPersonId(personId);
        clone.setUserOrgId(userOrgId);
        clone.setIp(ip);
        clone.setMac(mac);
        clone.setTerminalType(terminalType);

        clone.setAction(action);

        clone.setObjectId(objectId);
        clone.setObjectType(objectType);
        clone.setObjectName(objectName);

        clone.setOperationTime(operationTime);
        clone.setResult(result);
        clone.setDetailI18nKey(detailI18nKey);
        clone.setDetailItem(new LinkedList<>(detailItem));
        clone.setDetail(detail);

        clone.setServiceId(serviceId);
        clone.setTraceId(traceId);
        clone.setRelationId(relationId);
        clone.setErrorCode(errorCode);

        return clone;
    }
}
