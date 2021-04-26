package org.shoulder.ext.config.domain.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ConvertUtil;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.validate.util.ValidateUtil;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 配置数据
 *
 * @author lym
 */
public class ConfigData {

    /**
     * bizId 多 indexField 拼接分隔符
     */
    private static final String BIZ_ID_SPLIT = "#";

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 租户
     */
    private String tenant;

    /**
     * 业务数据对应的类名（不含包路径)
     */
    private ConfigType configType;

    /**
     * 业务索引哈希值（md5算法）固定长度32
     */
    private String bizId;

    /**
     * 版本号
     */
    private int version;

    /**
     * 操作人工号（通过buc接口获取）
     */
    private String operatorNo;

    /**
     * 操作人花名
     */
    private String operatorName;

    /**
     * 业务数据值
     */
    private Map<String, String> businessValue;

    /**
     * 配置类对象
     */
    private Object configObj;

    public ConfigData() {
    }

    public ConfigData(String tenant, ConfigType configType, Map<String, String> businessValue) {
        this.tenant = tenant;
        this.configType = configType;
        this.businessValue = businessValue;
        this.configObj = createConfigObjectFromFieldMap(configType, businessValue);
        ValidateUtil.validate(configObj);
        this.bizId = ConfigData.calculateBizId(tenant, configType, businessValue);
    }

    public ConfigData(String tenant, @Nonnull Object configObject) {
        AssertUtils.notNull(configObject, CommonErrorCodeEnum.UNKNOWN);
        this.configObj = configObject;
        ValidateUtil.validate(configObject);
        this.tenant = tenant;
        this.configType = ConfigType.getByType(configObject.getClass());
        this.bizId = calculateBizId(tenant, configObject);
        this.businessValue = extractFieldsFromConfigObject(configObject);
    }

    public ConfigData(String bizId, int version) {
        this.bizId = bizId;
        this.version = version;
    }

    /**
     * setter for column 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * getter for column 创建时间
     */
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * setter for column 修改时间
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * getter for column 修改时间
     */
    public Date getModifyTime() {
        return this.modifyTime;
    }

    /**
     * Getter method for property <tt>tenant</tt>.
     *
     * @return property value of tenant
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * Setter method for property <tt>tenant</tt>.
     *
     * @param tenant value to be assigned to property tenant
     */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public ConfigType getConfigType() {
        return configType;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param configType value to be assigned to property name
     */
    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }

    /**
     * setter for column 业务索引哈希值（md5算法）
     */
    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    /**
     * getter for column 业务索引哈希值（md5算法）
     */
    public String getBizId() {
        return this.bizId;
    }

    /**
     * setter for column 版本号
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * getter for column 版本号
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * setter for column 操作人工号（通过buc接口获取）
     */
    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    /**
     * getter for column 操作人工号（通过buc接口获取）
     */
    public String getOperatorNo() {
        return this.operatorNo;
    }

    /**
     * setter for column 操作人花名
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * getter for column 操作人花名
     */
    public String getOperatorName() {
        return this.operatorName;
    }

    /**
     * Getter method for property <tt>businessValue</tt>.
     *
     * @return property value of businessValue
     */
    public Map<String, String> getBusinessValue() {
        return businessValue;
    }

    /**
     * Setter method for property <tt>businessValue</tt>.
     *
     * @param businessValue value to be assigned to property businessValue
     */
    public void setBusinessValue(Map<String, String> businessValue) {
        this.businessValue = businessValue;
    }

    /**
     * Getter method for property <tt>configObj</tt>.
     *
     * @return property value of configObj
     */
    public Object getConfigObj() {
        return configObj;
    }

    /**
     * Setter method for property <tt>configObj</tt>.
     *
     * @param configObj value to be assigned to property configObj
     */
    public void setConfigObj(Object configObj) {
        this.configObj = configObj;
    }

    // ======================= 附属于该模型的业务方法 =========================

    /**
     * 索引键顺序拼接，调用前必须要保证已经校验
     * 考虑到tryCatch性能暂不合并重复代码
     *
     * @param tenant       租户
     * @param configObject configObj 必须是格式合法的
     * @return 返回长度一定是 32
     */
    public static String calculateBizId(String tenant, @Nonnull Object configObject) {
        ConfigType configType = ConfigType.getByType(configObject.getClass());
        try {
            StringBuilder bizId = new StringBuilder();
            bizId.append(tenant).append(BIZ_ID_SPLIT).append(configType.getConfigName()).append(BIZ_ID_SPLIT);
            for (ConfigFieldInfo fieldInfo : configType.getIndexFieldInfoList()) {
                // 无论什么类型，暂时都使用 String
                Object indexSegment = fieldInfo.getReadMethod().invoke(configObject);
                AssertUtils.notNull(indexSegment, ConfigErrorCodeEnum.CONFIG_DATA_MISS_BIZ_ID_FIELDS);
                String segment = String.valueOf(indexSegment);
                bizId.append(segment)
                        .append(BIZ_ID_SPLIT);
            }
            return DigestUtils.md5Hex(bizId.toString());
        } catch (Exception e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
    }

    /**
     * 索引键顺序拼接
     *
     * @param fieldMap 字段信息
     * @return 返回长度一定是 32
     */
    public static String calculateBizId(String tenant,
                                        ConfigType configType, Map<String, String> fieldMap) {
        return calculateBizId(tenant, configType, fieldMap::get);
    }

    /**
     * 索引键顺序拼接
     *
     * @param fieldValueCalculator 入参 字段属性名； 返回 属性值
     * @return 返回长度一定是 32
     */
    public static String calculateBizId(String tenant,
                                        ConfigType configType, Function<String, Object> fieldValueCalculator) {
        try {
            StringBuilder bizId = new StringBuilder();
            bizId.append(tenant).append(BIZ_ID_SPLIT).append(configType.getConfigName()).append(BIZ_ID_SPLIT);
            for (ConfigFieldInfo fieldInfo : configType.getIndexFieldInfoList()) {
                // 无论什么类型，暂时都使用 String
                Object indexSegment = fieldValueCalculator.apply(fieldInfo.getName());
                AssertUtils.notNull(indexSegment, ConfigErrorCodeEnum.CONFIG_DATA_MISS_BIZ_ID_FIELDS);
                String segment = String.valueOf(indexSegment);
                bizId.append(segment)
                        .append(BIZ_ID_SPLIT);
            }
            return DigestUtils.md5Hex(bizId.toString());
        } catch (Exception e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
    }

    /**
     * 提取配置类对象的属性，到 map
     *
     * @return map
     */
    public static Map<String, String> extractFieldsFromConfigObject(Object configObject) {
        ConfigType configType = ConfigType.getByType(configObject.getClass());
        Map<String, String> fieldMap = new HashMap<>(configType.getFieldInfoList().size());
        try {
            for (ConfigFieldInfo fieldInfo : configType.getFieldInfoList()) {
                // 无论什么类型，暂时都使用 String
                Object value = fieldInfo.getReadMethod().invoke(configObject);
                if (value != null) {
                    fieldMap.put(fieldInfo.getName(), String.valueOf(value));
                }
            }
            return fieldMap;
        } catch (Exception e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
    }

    /**
     * 根据map，提取配置类对象
     *
     * @return map
     */
    public static Object createConfigObjectFromFieldMap(ConfigType configType, Map<String, String> fieldMap) {
        try {
            Constructor<?> constructMethod = configType.getClazz().getConstructor();
            Object configObject = constructMethod.newInstance();
            for (ConfigFieldInfo fieldInfo : configType.getFieldInfoList()) {
                // 无论什么类型，暂时都使用 String
                String value = fieldMap.get(fieldInfo.getName());
                if (value != null) {
                    Object v = ConvertUtil.convert(value, fieldInfo.getType());
                    fieldInfo.getWriteMethod().invoke(configObject, v);
                }
            }
            return configObject;
        } catch (Exception e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
    }

}