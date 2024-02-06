package org.shoulder.ext.config.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.ext.config.domain.enums.ConfigOperationTypeEnum;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class ConfigHistoryLog {

    /**
     * 主键
     */
    protected Long id;

    /**
     * 创建时间
     */
    protected Instant gmtCreate;

    /**
     * 修改时间
     */
    protected Instant gmtModified;

    /**
     * 创建人
     */
    protected String creator;

    /**
     * 更新人
     */
    protected String modifier;

    /**
     * 数据版本号
     * 前端入口更新时必须判断数据版本号，后端任务/事件等间接更新不强求，但要打日志
     */
    protected Integer version;

    /**
     * 逻辑删除标志
     */
    private int deleteFlag;

    /**
     * 配置表主键
     */
    private String configBizId;

    /**
     * 操作类型，这条记录被如何改动进入的下个版本：UPDATE DELETE
     */
    private ConfigOperationTypeEnum operation;

    /**
     * 原业务数据内容，json格式
     */
    private Map<String, String> businessValue;

    public ConfigHistoryLog() {
    }

    public ConfigHistoryLog(ConfigData configData, ConfigOperationTypeEnum operation) {
        this.configBizId = configData.getBizId();
        this.version = configData.getVersion();
        this.operation = operation;
        this.businessValue = configData.getBusinessValue();
    }

}
