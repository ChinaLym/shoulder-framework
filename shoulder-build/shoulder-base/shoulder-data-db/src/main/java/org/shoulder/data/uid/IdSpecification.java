package org.shoulder.data.uid;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 流水号标准定义
 *
 * @author lym
 */
public interface IdSpecification {

    /**
     * 序列号-sequence的长度
     */
    int SEQUENCE_KEY_LENGTH = 8;

    /**
     * 分表位长度
     */
    int SPLIT_KEY_LENGTH = 2;

    /**
     * 长度不足时的填充字符
     */
    String COMPLETE_STR = "0";

    /**
     * 默认空字符串
     */
    String EMPTY_STR = "";

    /**
     * 默认业务类型代码
     */
    String DEFAULT_BIZ_CODE = COMPLETE_STR.repeat(3);

    /**
     * 默认租户编号
     */
    String DEFAULT_TENANT_CODE = COMPLETE_STR.repeat(3);

    /**
     * 默认用户非随机分表位
     */
    String NON_RANDOM_DEFAULT_USER_PARTITION = COMPLETE_STR.repeat(4);

    /**
     * 默认扩展位
     */
    String DEFAULT_EXTENSION_CODE = "";

    /**
     * standId 最小 length
     */
    int MIN_STAND_ID_LENGTH = 20;

    /**
     * 默认用户分表位
     */
    String DEFAULT_USER_PARTITION = COMPLETE_STR.repeat(2);

    /**
     * 标准流水号的用户分库分表起始位
     */
    int RANDOM_USER_DB_KEY_START = -10;

    /**
     * 标准流水号的用户分库分表结束位
     */
    int RANDOM_USER_DB_KEY_END = -8;

    /**
     * 数据版本起始位
     */
    int DATA_VERSION_START = 8;

    /**
     * 数据版本结束位
     */
    int DATA_VERSION_END = DATA_VERSION_START + 1;

    /**
     * 流水号的租户结束位
     */
    int SIT_ID_END = -12;

    /**
     * 流水号的租户起始位
     */
    int SIT_ID_START = -15;

    /**
     * 标准流水号的用户分库分表结束位
     */
    int USER_DB_KEY_END = -10;

    /**
     * 标准流水号的用户分库分表起始位
     */
    int USER_DB_KEY_START = -12;

    /**
     * 16位用户ID分库分表结束位
     */
    int USER_ID_START = -3;

    /**
     * 标准流水号的用户分库分表起始位
     */
    int USER_ID_END = -1;

    // ------------------------------------------------------------------------

    // standardize 校验，并转为标准id中的格式

    // extract 按照标准位置提取，并校验提取内容是标准格式

    /**
     * 校验，并转为标准id中的格式
     *
     * @param date 时间
     * @return 日期
     */
    static String standardizeDate(Date date) {
        //前8位yyyyMMdd格式
        //DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    static String standardizeDataVersion(String dataVersion) {
        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        AssertUtils.isTrue(dataVersion != null && dataVersion.length() == 1, CommonErrorCodeEnum.CODING, "sequence must > 0");
        return dataVersion;
    }

    static String standardizeSystemCode(String systemCode) {
        AssertUtils.isTrue(systemCode != null && systemCode.length() == 3, CommonErrorCodeEnum.CODING, "systemCode.length must = 3");
        return systemCode;
    }

    static String standardizeBizCode(String bizCode) {
        AssertUtils.isTrue(bizCode != null && bizCode.length() == 3, CommonErrorCodeEnum.CODING, "bizCode.length must = 3");
        //        bizCode = StringUtils.defaultIfBlank(bizCode, DEFAULT_BIZ_CODE);
        return bizCode;
    }

    static String standardizeEventCode(String eventCode) {
        AssertUtils.isTrue(eventCode != null && eventCode.length() == 8, CommonErrorCodeEnum.CODING, "eventCode.length must = 8");
        return eventCode;
    }

    static String standardizeExtension(String extensionCode, int maxLength) {
        AssertUtils.isTrue(extensionCode == null || extensionCode.length() == maxLength, CommonErrorCodeEnum.CODING,
            "extensionCode.length must <= ", maxLength);
        return StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);
    }

    static String standardizeTenantCode(String tenantCode) {

        tenantCode = StringUtils.defaultIfBlank(tenantCode, DEFAULT_TENANT_CODE);
        AssertUtils.isTrue(tenantCode.length() == 3, CommonErrorCodeEnum.CODING, "tenantCode.length must = 3");
        return tenantCode;
    }

    /**
     * 获取2位标准的分表位，长度不足前面补0
     *
     * @param sharding 分表位
     * @return 标准长度分表位
     */
    static String standardizeSharding(String sharding) {
        //如果大于length位，则截取低length位
        if (sharding.length() > IdSpecification.SPLIT_KEY_LENGTH) {
            sharding = StringUtils.substring(sharding, -IdSpecification.SPLIT_KEY_LENGTH);
        }

        return StringUtils.alignRight(sharding, IdSpecification.SPLIT_KEY_LENGTH, COMPLETE_STR);
    }

    static String standardizeUserRandomSharding(String userRandomPartition) {
        if (StringUtils.isBlank(userRandomPartition) || userRandomPartition.length() != 2) {
            Random random = new Random();
            userRandomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }
        return userRandomPartition;
    }

    /**
     * 获取8位的标准sequence,长度不足前面补0
     *
     * @param seq sequence
     * @return 标准8位sequence
     */
    static String standardizeSequence(long seq) {
        return adjustSequenceLength(seq, SEQUENCE_KEY_LENGTH, COMPLETE_STR);
    }

    /**
     * 获取 length 位的sequence,长度不足前面补0
     *
     * @param seq    sequence
     * @param length 长度
     * @return 标准8位sequence
     */
    static String adjustSequenceLength(long seq, int length, String completeStr) {
        AssertUtils.isTrue(seq > 0, CommonErrorCodeEnum.CODING, "sequence must > 0");

        String seqStr = String.valueOf(seq);
        //如果大于length位，则截取低length位
        if (seqStr.length() > length) {
            seqStr = StringUtils.substring(seqStr, -length);
        }
        return StringUtils.alignRight(seqStr, length, completeStr);
    }

    static void validateStandardId(String standardId) {
        AssertUtils.isTrue(standardId != null && standardId.length() > MIN_STAND_ID_LENGTH, CommonErrorCodeEnum.CODING,
            "standardId.length must > " + MIN_STAND_ID_LENGTH);
        // todo P2 region
        extractRegionCode(standardId);
    }

    /**
     * 根据 userId 获取用户分库分表位，倒数二三位
     *
     * @param userId userId,一般16位
     * @return 用户分库分表位 userId.subString(-3, -1)
     */
    static String extractUserShardingFromUserIdOrDefault(String userId) {
        if (StringUtils.isBlank(userId) || userId.length() < 3) {
            return DEFAULT_USER_PARTITION;
        }
        return StringUtils.substring(userId, USER_ID_START, USER_ID_END);
    }

    /**
     * 根据关联单号（标准流水号），获取用户分库分表位
     *
     * @param standardId 关联单号 必须符合标准流水号的倒数第11和12位
     * @return 用户分库分表位
     */
    static String extractUserShardingFromStandardId(String standardId) {
        validateStandardId(standardId);
        String userSharding = StringUtils.substring(standardId, USER_DB_KEY_START, USER_DB_KEY_END);
        AssertUtils.notEmpty(userSharding, CommonErrorCodeEnum.CODING, "standardId invalid.");
        return userSharding;
    }

    /**
     * 根据关联单号，获取用户随机分表位，, standardId 必须符合标准流水标准，倒数第9和第10位
     *
     * @param standardId 关联单号
     * @return 用户分库分表位
     */
    static String extractRandomShardingFromStandardId(String standardId) {
        validateStandardId(standardId);
        return StringUtils.substring(standardId, RANDOM_USER_DB_KEY_START, RANDOM_USER_DB_KEY_END);
    }

    /**
     * 获取标准流水号的租户码，在倒数第13和第16位
     *
     * @param standardId 标准流水号
     * @return 租户码
     */
    static String extractTenantCodeFromStandardId(String standardId) {
        validateStandardId(standardId);
        String tenantCode = StringUtils.substring(standardId, SIT_ID_START, SIT_ID_END);
        return standardizeTenantCode(tenantCode);
    }

    /**
     * 根据用户ID，获取用户分库分表位，即用户UID倒数三二位+倒数五四位
     * 例如00054321获取到的分库分表段为3254
     *
     * @param userId 用户ID
     * @return 用户分库分表位
     */
    static String extractUserRandomShardingFromUserIdOrDefault(String userId) {
        if (StringUtils.isBlank(userId) || userId.length() < 5) {
            return NON_RANDOM_DEFAULT_USER_PARTITION;
        }
        String userSharding = extractUserShardingFromUserIdOrDefault(userId);
        String userRandomSharding = StringUtils.substring(userId, -5, -3);
        return userSharding + userRandomSharding;
    }

    /**
     * 获取标准流水号中的数据版本位。
     *
     * @param standardId 标准流水号
     * @return 单号版本位
     */
    static String extractDataVersionFromStandardId(String standardId) {

        return StringUtils.substring(standardId, DATA_VERSION_START, DATA_VERSION_END);
    }

    /**
     * 获取业务标识码。
     *
     * @param standardId 标准流水号
     * @return 业务标识码
     */
    static String extractBizCode(String standardId) {
        return StringUtils.substring(standardId, 13, 19);
    }

    static String extractRegionCode(String standardId) {
        AssertUtils.notBlank(standardId, CommonErrorCodeEnum.ILLEGAL_PARAM);

        String regionCode = StringUtils.substring(standardId, 9, 10);
        validateRegionCode(regionCode);
        return regionCode;
    }

    // ----------------------------------------------------------------

    static String getCurrentRegionCode() {
        // todo P2 region
        //return AppInfo.xxx;
        return "000";
    }

    static void validateRegionCode(String regionCode) {
        AssertUtils.notNull(regionCode, CommonErrorCodeEnum.CODING, "sequence must > 0");
        // todo P2 region enum check
    }
}
