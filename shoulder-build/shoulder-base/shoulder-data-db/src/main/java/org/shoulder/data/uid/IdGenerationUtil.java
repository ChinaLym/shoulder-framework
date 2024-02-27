package org.shoulder.data.uid;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 流水号生成工具
 *
 * @author lym
 */
public class IdGenerationUtil {

    /**
     * 序列号-sequence的长度
     */
    private static final int SEQUENCE_KEY_LENGTH = 8;

    /**
     * 分表位长度
     */
    private static final int SPLIT_KEY_LENGTH = 2;

    /**
     * 长度不足时的填充字符
     */
    private static final String COMPLETE_STR = "0";

    /**
     * 默认空字符串
     */
    private static final String EMPTY_STR = "";

    /**
     * 默认业务类型代码
     */
    private static final String DEFAULT_BIZ_CODE = COMPLETE_STR.repeat(3);

    /**
     * 默认站点编号
     */
    private static final String DEFAULT_SITE_ID = COMPLETE_STR.repeat(3);

    /**
     * 默认用户非随机分表位
     */
    private static final String NON_RANDOM_DEFAULT_USER_PARTITION = COMPLETE_STR.repeat(4);

    /**
     * 默认扩展位
     */
    private static final String DEFAULT_EXTENSION_CODE = "";

    /**
     * 默认用户分表位
     */
    private static final String DEFAULT_USER_PARTITION = COMPLETE_STR.repeat(2);

    /**
     * 标准流水号的用户分库分表起始位
     */
    private static final int RONDOM_USER_DB_KEY_START = -10;

    /**
     * 标准流水号的用户分库分表结束位
     */
    private static final int RONDOM_USER_DB_KEY_END = -8;

    /**
     * 数据版本起始位
     */
    private static final int DATA_VERSION_START = 8;

    /**
     * 数据版本结束位
     */
    private static final int DATA_VERSION_END = DATA_VERSION_START + 1;

    /**
     * 流水号的站点结束位
     */
    private static final int SIT_ID_END = -12;

    /**
     * 流水号的站点起始位
     */
    private static final int SIT_ID_START = -15;

    /**
     * 标准流水号的用户分库分表结束位
     */
    private static final int USER_DB_KEY_END = -10;

    /**
     * 标准流水号的用户分库分表起始位
     */
    private static final int USER_DB_KEY_START = -12;

    /**
     * 16位用户ID分库分表结束位
     */
    private static final int USER_ID_START = -3;

    /**
     * 标准流水号的用户分库分表起始位
     */
    private static final int USER_ID_END = -1;

    /**
     * 普通主键补足,非关键流水ID可使用此生成方法,比如一些后台系统，配置表生成主键
     * <p>
     * 格式：日期(8)+数据版本位(1)+区域位(1)+sequence(8)
     *
     * @param now         当前时间，必填
     * @param dataVersion 非必填，默认是1
     * @param seq         sequence，必填，标准长度是8位，超过8位截取后八位，不足八位前面补齐0
     * @return id  流水号ID
     */
    public static String generateId(Date now, String dataVersion, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }
        String today = getPartition(now);
        return today + dataVersion + getCurrentRegionCode() + realSeq;
    }

    /**
     * 普通主键补足,非关键流水ID可使用此生成方法,比如一些后台系统，配置表生成主键
     * <p>
     * 格式：日期(8)+数据版本位(1)+区域位(1)+站点位(3)+sequence(8)
     *
     * @param now         当前时间，必填
     * @param dataVersion 非必填，默认是1
     * @param tntInstId   必填
     * @param seq         sequence，必填，标准长度是8位，超过8位截取后八位，不足八位前面补齐0
     * @return id  流水号ID
     */
    public static String generateId(Date now, String dataVersion, String tntInstId, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        if (StringUtils.isBlank(tntInstId)) {
            throw new RuntimeException(" tntInstId is null");
        }

        String today = getPartition(now);
        return today + dataVersion + getCurrentRegionCode() + tntInstId + realSeq;
    }

    /**
     * 普通主键补足,非关键流水ID可使用此生成方法,比如一些后台系统，配置表生成主键
     * <p>
     * 资金平台的应用流水表对应的业务流水记录很多都是id主键，类型是bigint最长长度为19位（2的63次方-1），
     * 且OB不支持修改字段类型，而目前标准字符串类型的业务流水号为37~64位，
     * 不能满足需求。对资金平台原有表的自增ID的改造只能去掉自增生成，并且新的ID不能超过19位；
     * <p>
     * 格式：日期(8)+区域位(1)+分表位(2)+sequence(8)
     *
     * @param now   当前时间，必填
     * @param split 分表位【00~99】必填，不足两位前面补0
     * @param seq   sequence，必填，标准长度是8位，超过8位截取后八位，不足八位前面补齐0
     * @return id  流水号ID
     */
    public static Long generateNumberId(Date now, String split, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }
        //格式化日期
        String today = getPartition(now);

        //分表位不足两位补0
        String formatSplit = getStandardSplit(split, SPLIT_KEY_LENGTH);

        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        return Long.parseLong(today + getCurrentRegionCode() + formatSplit + realSeq);
    }

    /**
     * SGP机房建设后，新的主键补足，主要是去除了系统版本位，并用区域码取而代之<br>
     * <li>要符合支付宝三代架构中有关流水ID的数据架构规范标准<br>
     * <li>所有流水生成必须使用该方法<br>
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>...</td> <td>12</td> <td>11</td> <td>10</td>
     * <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>1</td> <td>1</td>
     * <td>0</td> <td>1</td> <td>9</td> <td>0</td> <td>0</td> <td>0</td>  <td>...</td>  <td>4</td> <td>4</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">R</td> <td bgcolor="#8e44ad">S</td> <td bgcolor="#8e44ad">S</td> <td
     * bgcolor="#8e44ad">S</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td
     * bgcolor="#0eef8e">E</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填默认1
     * @param systemCode         系统标志码，必填
     * @param bizCode            业务标志码，不填默认000
     * @param extensionCode      扩展码，长度小于等于32位,可不填
     * @param accountNo          账号或用户ID
     * @param userRadomPartition 随机用户dbkey,若不指定，随机产生两位，一般用于流水号FO
     * @param seq                序列号
     * @return 流水号ID
     */
    public static String generateId(Date now, String dataVersion, String systemCode,
                                    String bizCode, String extensionCode, String accountNo,
                                    String userRadomPartition, long seq) {

        //入参检查
        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        if (StringUtils.isNotBlank(systemCode) && systemCode.length() != 3) {
            throw new RuntimeException("系统码不合法");
        }

        if (StringUtils.isNotBlank(bizCode) && bizCode.length() != 3) {
            throw new RuntimeException("业务标识码不符合规范");
        }

        if (StringUtils.isNotBlank(extensionCode) && extensionCode.length() > 32) {
            throw new RuntimeException("扩展位不符合规范");
        }

        //日期位
        String today = getPartition(now);

        //不足8位的补0
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        //数据版本
        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        //业务标识码,若空默认000
        bizCode = StringUtils.defaultIfBlank(bizCode, DEFAULT_BIZ_CODE);

        //扩展标识码，若不填，则""
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        //用户分库分表位
        String userPartition = getUserPartitionByUserId(accountNo);

        //客户端若指定2位用户随机DB位，一般用于FO.若不指定，随机产生两位
        if (StringUtils.isBlank(userRadomPartition) || userRadomPartition.length() != 2) {
            Random random = new Random();
            userRadomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }
        return today + dataVersion + getCurrentRegionCode() + systemCode + bizCode
               + extensionCode + userPartition + userRadomPartition + realSeq;
    }

    /**
     * SGP机房建设后，新的主键补足，主要是去除了系统版本位，并用区域码取而代之<br>
     * <li>要符合支付宝三代架构中有关流水ID的数据架构规范标准<br>
     * <li>所有流水生成必须使用该方法<br>
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>...</td> <td>12</td> <td>11</td> <td>10</td>
     * <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>1</td> <td>1</td>
     * <td>0</td> <td>1</td> <td>9</td> <td>0</td> <td>0</td> <td>0</td>  <td>...</td>  <td>4</td> <td>4</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">R</td> <td bgcolor="#8e44ad">S</td> <td bgcolor="#8e44ad">S</td> <td
     * bgcolor="#8e44ad">S</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td
     * bgcolor="#0eef8e">E</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填默认1
     * @param systemCode         系统标志码，必填
     * @param bizCode            业务标志码，不填默认空
     * @param eventCode          业务事件码，不填默认空
     * @param extensionCode      扩展码，长度小于等于32位,可不填
     * @param siteId             站点位
     * @param accountNo          账号或用户ID
     * @param userRadomPartition 随机用户dbkey,若不指定，随机产生两位，一般用于流水号FO
     * @param seq                序列号
     * @return 流水号ID
     *
     * 【备注】: 事件码默认空为空字符串 ，generateId方法中有bug，线上大量业务系统使用该方法，无法准确评估更改后业务影响，
     * 故新加 generateIdV2 方法，已修复该bug，不强制旧系统更改，新系统推荐用V2版本方法。固该有 bug 方法打上废弃标识。
     */
    @Deprecated
    public static String generateId(Date now, String dataVersion, String systemCode,
                                    String bizCode, String eventCode, String extensionCode, String siteId,
                                    String accountNo, String userRadomPartition, long seq) {

        //入参检查
        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        if (StringUtils.isNotBlank(systemCode) && systemCode.length() != 3) {
            throw new RuntimeException("系统码不合法");
        }

        if (StringUtils.isNotBlank(bizCode) && bizCode.length() != 3) {
            throw new RuntimeException("业务标识码不符合规范");
        }

        if (StringUtils.isNotBlank(eventCode) && eventCode.length() != 8) {
            throw new RuntimeException("事件码不符合规范");
        }

        if (StringUtils.isNotBlank(extensionCode) && extensionCode.length() > 32) {
            throw new RuntimeException("扩展位不符合规范");
        }

        siteId = StringUtils.defaultIfBlank(siteId, DEFAULT_SITE_ID);
        if (siteId.length() != 3) {
            throw new RuntimeException("invalid siteId");
        }

        //日期位
        String today = getPartition(now);

        //不足8位的补0
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        //数据版本
        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        //业务标识码,若空默认空字符串
        bizCode = StringUtils.defaultIfBlank(bizCode, EMPTY_STR);

        //事件码默认空为空字符串
        eventCode = StringUtils.defaultIfBlank(eventCode, DEFAULT_SITE_ID);

        //扩展标识码，若不填，则""
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        //用户分库分表位
        String userPartition = getUserPartitionByUserId(accountNo);

        //客户端若指定2位用户随机DB位，一般用于FO.若不指定，随机产生两位
        if (StringUtils.isBlank(userRadomPartition) || userRadomPartition.length() != 2) {
            Random random = new Random();
            userRadomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }
        return today + dataVersion + getCurrentRegionCode() + systemCode + bizCode
               + eventCode + extensionCode + siteId + userPartition + userRadomPartition + realSeq;
    }

    /**
     * SGP机房建设后，新的主键补足，主要是去除了系统版本位，并用区域码取而代之<br>
     * <li>要符合支付宝三代架构中有关流水ID的数据架构规范标准<br>
     * <li>所有流水生成必须使用该方法<br>
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>...</td> <td>12</td> <td>11</td> <td>10</td>
     * <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>1</td> <td>1</td>
     * <td>0</td> <td>1</td> <td>9</td> <td>0</td> <td>0</td> <td>0</td>  <td>...</td>  <td>4</td> <td>4</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">R</td> <td bgcolor="#8e44ad">S</td> <td bgcolor="#8e44ad">S</td> <td
     * bgcolor="#8e44ad">S</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td
     * bgcolor="#0eef8e">E</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填默认1
     * @param systemCode         系统标志码，必填
     * @param bizCode            业务标志码，不填默认空
     * @param eventCode          业务事件码，不填默认空
     * @param extensionCode      扩展码，长度小于等于32位,可不填
     * @param siteId             站点位
     * @param accountNo          账号或用户ID
     * @param userRadomPartition 随机用户dbkey,若不指定，随机产生两位，一般用于流水号FO
     * @param seq                序列号
     * @return 流水号ID
     *
     * 【备注】: 事件码默认空为空字符串 ，generateId方法中有bug，线上大量业务系统使用该方法，无法准确评估更改后业务影响，
     * 故新加 generateIdV2 方法，已修复该bug，不强制旧系统更改，新系统推荐用V2版本方法。固该有 bug 方法打上废弃标识。
     */
    public static String generateIdV2(Date now, String dataVersion, String systemCode,
                                      String bizCode, String eventCode, String extensionCode, String siteId,
                                      String accountNo, String userRadomPartition, long seq) {

        //入参检查
        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        if (StringUtils.isNotBlank(systemCode) && systemCode.length() != 3) {
            throw new RuntimeException("系统码不合法");
        }

        if (StringUtils.isNotBlank(bizCode) && bizCode.length() != 3) {
            throw new RuntimeException("业务标识码不符合规范");
        }

        if (StringUtils.isNotBlank(eventCode) && eventCode.length() != 8) {
            throw new RuntimeException("事件码不符合规范");
        }

        if (StringUtils.isNotBlank(extensionCode) && extensionCode.length() > 32) {
            throw new RuntimeException("扩展位不符合规范");
        }

        siteId = StringUtils.defaultIfBlank(siteId, DEFAULT_SITE_ID);
        if (siteId.length() != 3) {
            throw new RuntimeException("invalid siteId");
        }

        //日期位
        String today = getPartition(now);

        //不足8位的补0
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        //数据版本
        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        //业务标识码,若空默认空字符串
        bizCode = StringUtils.defaultIfBlank(bizCode, EMPTY_STR);

        //事件码默认空为空字符串
        eventCode = StringUtils.defaultIfBlank(eventCode, EMPTY_STR);

        //扩展标识码，若不填，则""
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        //用户分库分表位
        String userPartition = getUserPartitionByUserId(accountNo);

        //客户端若指定2位用户随机DB位，一般用于FO.若不指定，随机产生两位
        if (StringUtils.isBlank(userRadomPartition) || userRadomPartition.length() != 2) {
            Random random = new Random();
            userRadomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }
        return today + dataVersion + getCurrentRegionCode() + systemCode + bizCode
               + eventCode + extensionCode + siteId + userPartition + userRadomPartition + realSeq;
    }

    /**
     * 根据已经生成的关联流水号，生成同样分库分表的新流水号
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>...</td> <td>12</td> <td>11</td> <td>10</td>
     * <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>1</td> <td>1</td>
     * <td>0</td> <td>1</td> <td>9</td> <td>0</td> <td>0</td> <td>0</td>  <td>...</td>  <td>4</td> <td>4</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">R</td> <td bgcolor="#8e44ad">S</td> <td bgcolor="#8e44ad">S</td> <td
     * bgcolor="#8e44ad">S</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td
     * bgcolor="#0eef8e">E</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now           当前时间，必填
     * @param dataVersion   数据版本，不填默认1
     * @param systemCode    系统标志码，必填
     * @param bizCode       业务标志码，不填默认000
     * @param extensionCode 扩展码，长度小于32位，可不填
     * @param relatedKey    业务关联key
     * @param seq           序列号  
     * @return 流水号ID
     */
    public static String generateIdByRelatedKey(Date now, String dataVersion, String systemCode,
                                                String bizCode, String extensionCode,
                                                String relatedKey, long seq) {

        String dbPrimaryKey = generateId(now, dataVersion, systemCode, bizCode, extensionCode,
            null, null, seq);

        String userDBKey = getUserPartitionByRelatedKey(relatedKey);

        if (StringUtils.isBlank(userDBKey)) {
            throw new RuntimeException("relatedKey is invalid");
        }

        String userRadomPartition = getRandomPartionByRelatedKey(relatedKey);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + userDBKey
               + userRadomPartition + StringUtils.substring(dbPrimaryKey, RONDOM_USER_DB_KEY_END);
    }


    /**
     * 无用户id，并指定分库分表，生成新的流水ID
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>...</td> <td>12</td> <td>11</td> <td>10</td>
     * <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td> <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>1</td> <td>1</td>
     * <td>0</td> <td>1</td> <td>9</td> <td>0</td> <td>0</td> <td>0</td>  <td>...</td>  <td>4</td> <td>4</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">R</td> <td bgcolor="#8e44ad">S</td> <td bgcolor="#8e44ad">S</td> <td
     * bgcolor="#8e44ad">S</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td bgcolor="#3498db">B</td> <td
     * bgcolor="#0eef8e">E</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填默认1
     * @param systemCode         系统标志码，必填
     * @param bizCode            业务标志码，不填默认000
     * @param extensionCode      扩展码，长度小于等于32位，可不填
     * @param partitionKey       指定用户分库
     * @param userRadomPartition 指定用户分库随机位
     * @param seq                序列号
     * @return 流水号ID
     */
    public static String generateIdByPartitionKey(Date now, String dataVersion, String systemCode,
                                                  String bizCode, String extensionCode,
                                                  String partitionKey, String userRadomPartition,
                                                  long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateId(now, dataVersion, systemCode, bizCode, extensionCode,
            null, userRadomPartition, seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
               + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    /**
     * 生成35位ID
     * <li> 必须符合三代架构规范
     * <li> 新增站点、业务事件码，但仍符合三代架构规范
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td> <td>15</td>
     * <td>14</td> <td>13</td> <td>12</td> <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td></td> <td>0</td> <td>0</td> <td>1</td>
     * <td>4</td> <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td></td> <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td> <td
     * bgcolor="#3498db">S</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">站点标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填，默认为1
     * @param eventCode          事件码，必
     * @param extensionCode      扩展字段: <=32位
     * @param siteId             站点标识
     * @param userId             用户分表字段
     * @param userRadomPartition 用户随机位
     * @param seq                流水号
     * @return 标准流水号生成规则
     */
    public static String generateIdForAPlus(Date now, String dataVersion, String eventCode,
                                            String extensionCode, String siteId, String userId,
                                            String userRadomPartition, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String today = getPartition(now);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        if (eventCode == null || eventCode.length() != 8) {
            throw new RuntimeException("invalid eventCode");
        }

        if (extensionCode != null && extensionCode.length() > 32) {
            throw new RuntimeException("extensionCode is too long");
        }
        //扩展位默认不填就""
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        siteId = StringUtils.defaultIfBlank(siteId, DEFAULT_SITE_ID);
        if (siteId.length() != 3) {
            throw new RuntimeException("invalid siteId");
        }
        String userDBKey = getUserPartitionByUserId(userId);

        //客户端指定2位DBkey，若不指定，随机产生两位
        if (StringUtils.isBlank(userRadomPartition) || userRadomPartition.length() != 2) {
            Random random = new Random();
            userRadomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }

        //如果不满8位,左边补"0"
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        return today + dataVersion + getCurrentRegionCode() + eventCode
               + extensionCode + siteId + userDBKey + userRadomPartition + realSeq;
    }

    /**
     * 生成ID,relatedKey需要符合35位标准
     *
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td> <td>15</td>
     * <td>14</td> <td>13</td> <td>12</td> <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td></td> <td>0</td> <td>0</td> <td>1</td>
     * <td>4</td> <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td></td> <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td> <td
     * bgcolor="#3498db">S</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">站点标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">分库位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now           当前时间
     * @param dataVersion   数据版本: 1位
     * @param eventCode     事件码: 8位
     * @param extensionCode 扩展字段: <=32位
     * @param relatedKey    关联流水号
     * @param seq           流水号
     * @return 标准流水号
     */
    public static String generateIdByRelateKeyForAPlus(Date now, String dataVersion,
                                                       String eventCode, String extensionCode,
                                                       String relatedKey, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String today = getPartition(now);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        if (eventCode == null || eventCode.length() != 8) {
            throw new RuntimeException("invalid eventCode");
        }

        String regionCode = parseRegionCode(relatedKey);
        if (StringUtils.isBlank(regionCode) || !isValidRegionCode(regionCode)) {
            throw new RuntimeException("invalid regionCode");
        }

        if (extensionCode != null && extensionCode.length() > 32) {
            throw new RuntimeException("extensionCode is too long");
        }
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        if (relatedKey == null) {
            throw new RuntimeException("  invalid relatedKey ");
        }

        String siteId = getSitIdbyRelatedKey(relatedKey);
        String userDBKey = getUserPartitionByRelatedKey(relatedKey);
        String userRadomPartition = getRandomPartionByRelatedKey(relatedKey);
        // 校验传入的dbKey的长度
        if (userRadomPartition == null || userRadomPartition.length() != 2) {
            throw new RuntimeException("invalid dbKey.");
        }

        //如果不满8位,左边补"0"
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        return today + dataVersion + regionCode + eventCode + extensionCode + siteId + userDBKey
               + userRadomPartition + realSeq;
    }

    /**
     * 生成35位ID
     * <li> 必须符合三代架构规范
     * <li> 新增站点、业务事件码，但仍符合三代架构规范
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td> <td>15</td>
     * <td>14</td> <td>13</td> <td>12</td> <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td></td> <td>0</td> <td>0</td> <td>1</td>
     * <td>4</td> <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td></td> <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td> <td
     * bgcolor="#3498db">S</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">站点标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填，默认为1
     * @param eventCode          事件码，必
     * @param extensionCode      扩展字段: <=32位
     * @param siteId             站点标识
     * @param partitionKey       用户指定分库分表字段
     * @param userRadomPartition 用户随机位
     * @param seq                流水号
     * @return 标准流水号生成规则
     */
    public static String generateIdByPartitionKeyForAplus(Date now, String dataVersion, String eventCode,
                                                          String extensionCode, String siteId, String partitionKey,
                                                          String userRadomPartition, long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateIdForAPlus(now, dataVersion, eventCode, extensionCode, siteId, null, userRadomPartition, seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
               + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    /**
     * 生成ID，分库分表位是userID倒数2、3位，用户随机分表位是倒数4、5位
     *
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td> <td>15</td>
     * <td>14</td> <td>13</td> <td>12</td> <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td></td> <td>0</td> <td>0</td> <td>1</td>
     * <td>4</td> <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td></td> <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td> <td
     * bgcolor="#3498db">S</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td
     * bgcolor="#2ecc71">U</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">站点标示位</td> <td colspan="4">客户分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now           当前时间
     * @param dataVersion   数据版本: 1位
     * @param eventCode     事件码: 8位
     * @param extensionCode 扩展字段: <=32位
     * @param siteId        站点标识
     * @param userId        用户分表字段
     * @param seq           流水号
     * @return id
     */
    public static String generateIDWithNoRandomUserRule(Date now, String dataVersion,
                                                        String eventCode, String extensionCode,
                                                        String siteId, String userId, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String today = getPartition(now);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        if (eventCode == null || eventCode.length() != 8) {
            throw new RuntimeException("invalid eventCode");
        }

        if (extensionCode != null && extensionCode.length() > 32) {
            throw new RuntimeException("extensionCode is too long");
        }
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        siteId = StringUtils.defaultIfBlank(siteId, DEFAULT_SITE_ID);
        String userDBKey = getNoRandomUserRuleFromUserId(userId);
        //如果不满8位,左边补"0"
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        return today + dataVersion + getCurrentRegionCode() + eventCode
               + extensionCode + siteId + userDBKey + realSeq;
    }

    /**
     * 获取8位的标准sequence,长度不足前面补0
     *
     * @param seq    sequence
     * @param length 长度
     * @return 标准8位sequence
     */
    public static String getStandardSeq(long seq, int length) {

        String seqStr = String.valueOf(seq);
        //如果大于length位，则截取低length位
        if (seqStr.length() > length) {
            seqStr = StringUtils.substring(seqStr, -length);
        }
        return StringUtils.alignRight(seqStr, 8, COMPLETE_STR);
    }

    /**
     * 获取2位标准的分表位，长度不足前面补0
     *
     * @param split
     * @param length
     * @return
     */
    private static String getStandardSplit(String split, int length) {
        //如果大于length位，则截取低length位
        if (split.length() > length) {
            split = StringUtils.substring(split, -length);
        }

        return StringUtils.alignRight(split, 2, COMPLETE_STR);
    }

    /**
     * 日期转换。
     *
     * @param date 时间
     * @return 日期
     */
    public static String getPartition(Date date) {
        //前8位yyyyMMdd格式
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    /**
     * 根据关联单号，获取用户分库分表位，即用户UID倒数二三位,userID一般16位
     *
     * @param userId 关联单号
     * @return 用户分库分表位
     */
    public static String getUserPartitionByUserId(String userId) {
        if (StringUtils.isBlank(userId) || userId.length() < 3) {
            return DEFAULT_USER_PARTITION;
        }
        return StringUtils.substring(userId, USER_ID_START, USER_ID_END);
    }

    /**
     * 根据关联单号，获取用户分库分表位，relatedkey必须符合三点架构流水标准，倒数第11和12位
     *
     * @param relatedKey 关联单号
     * @return 用户分库分表位
     */
    public static String getUserPartitionByRelatedKey(String relatedKey) {
        return StringUtils.substring(relatedKey, USER_DB_KEY_START, USER_DB_KEY_END);
    }

    /**
     * 根据关联单号，获取用户随机分表位，,relatedkey必须符合三点架构流水标准，倒数第9和第10位
     *
     * @param relatedKey 关联单号
     * @return 用户分库分表位
     */
    public static String getRandomPartionByRelatedKey(String relatedKey) {
        return StringUtils.substring(relatedKey, RONDOM_USER_DB_KEY_START, RONDOM_USER_DB_KEY_END);
    }

    /**
     * 标准流水号好中的站点位置，在倒数第13和第16位
     *
     * @param relatedKey
     * @return 站点码
     */
    public static String getSitIdbyRelatedKey(String relatedKey) {
        return StringUtils.substring(relatedKey, SIT_ID_START, SIT_ID_END);
    }

    /**
     * 根据用户ID，获取用户分库分表位，即用户UID倒数三二位+倒数五四位
     * 例如00067891获取到的分库分表段为8967
     *
     * @param userId 用户ID
     * @return 用户分库分表位
     */
    public static String getNoRandomUserRuleFromUserId(String userId) {
        if (StringUtils.isBlank(userId) || userId.length() < 5) {
            return NON_RANDOM_DEFAULT_USER_PARTITION;
        }
        String userSharding = getUserPartitionByUserId(userId);
        String userRadomPartition = StringUtils.substring(userId, -5, -3);
        return userSharding + userRadomPartition;
    }

    /**
     * 获取主键单号中的数据版本位。
     *
     * @param primaryKey 主键单号
     * @return 单号版本位
     */
    public static String getDataVersion(String primaryKey) {

        return StringUtils.substring(primaryKey, DATA_VERSION_START, DATA_VERSION_END);
    }


    /**
     * 获取业务标识码。
     *
     * @param key 主键单号
     * @return 业务标识码
     */
    public static String getBizCode(String key) {

        if (28 == key.length()) {
            return StringUtils.substring(key, 13, 16);
        }

        return StringUtils.substring(key, 13, 19);
    }

    /**
     * 生成，支持主体隔离的37位ID
     * <li> 必须符合三代架构规范
     * <li> 新增站点、业务事件码，但仍符合三代架构规范
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td>
     *
     * <td>19</td><td>18</td> <td>17</td> <td>16</td>
     * <td>15</td><td>14</td> <td>13</td> <td>12</td>
     * <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>...</td>
     *
     * <td>3</td> <td>0</td> <td>8</td><td>8</td>
     * <td>0</td> <td>0</td> <td>1</td><td>4</td>
     * <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8080C0">...</td>
     *
     * <td bgcolor="#8080C0">L</td> <td bgcolor="#8080C0">L</td>
     * <td bgcolor="#8080C0">L</td> <td bgcolor="#8080C0">L</td>
     *
     * <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td>
     * <td
     * bgcolor="#3498db">S</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td>
     * <td colspan="4">法律主体码</td>
     * <td colspan="3">站点标示位</td>
     * <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填，默认为1
     * @param eventCode           事件码，必
     * @param extensionCode       扩展字段: <=27位
     * @param legalInst           法律主体码
     * @param siteId              站点标识
     * @param partitionKey        用户指定分库分表字段
     * @param userRandomPartition 用户随机位
     * @param seq                 流水号
     * @return 流水号ID
     */
    public static String generateIdByPartitionKeyForAplus(Date now, String dataVersion, String eventCode,
                                                          String extensionCode, String legalInst, String siteId, String partitionKey,
                                                          String userRandomPartition, long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateIdForAPlus(now, dataVersion, eventCode, extensionCode, legalInst, siteId, null, userRandomPartition,
            seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
               + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    /**
     * 生成，支持主体隔离的37位ID
     * <li> 必须符合三代架构规范
     * <li> 新增站点、业务事件码，但仍符合三代架构规范
     * <table border="1" style="border-color: #bdc3c7">
     * <tr>
     * <td>位置</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td> <td>9</td> <td>10</td>
     * <td>11</td> <td>12</td> <td>13</td> <td>14</td> <td>15</td> <td>16</td> <td>17</td> <td>18</td> <td>...</td>
     * <td>19</td><td>18</td> <td>17</td> <td>16</td><td>15</td>
     * <td>14</td> <td>13</td> <td>12</td> <td>11</td> <td>10</td> <td>9</td> <td>8</td> <td>7</td> <td>6</td> <td>5</td> <td>4</td>
     * <td>3</td> <td>2</td> <td>1</td>
     * </tr>
     * <tr>
     * <td>示例</td> <td>y</td> <td>y</td> <td>y</td> <td>y</td> <td>M</td> <td>M</td> <td>d</td> <td>d</td> <td>0</td> <td>0</td>
     * <td>1</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td>0</td> <td></td> <td>0</td> <td>0</td> <td>1</td>
     * <td>4</td> <td>4</td> <td>0</td> <td>0</td> <td>1</td> <td>2</td> <td>3</td> <td>4</td> <td>5</td> <td>6</td> <td>7</td> <td>8</td>
     * </tr>
     * <tr>
     * <td></td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td bgcolor="#f1c40f">D</td> <td
     * bgcolor="#bdc3c7">V</td> <td bgcolor="#bdc3c7">V</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td
     * bgcolor="#8e44ad">E</td> <td bgcolor="#8e44ad">E</td> <td></td>
     *
     * <td bgcolor="#3498db">L</td> <td bgcolor="#3498db">L</td> <td bgcolor="#3498db">L</td> <td bgcolor="#3498db">L</td>
     * <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td> <td bgcolor="#3498db">S</td>
     * <td bgcolor="#2ecc71">U</td> <td bgcolor="#2ecc71">U</td> <td bgcolor="#e67e22">R</td> <td
     * bgcolor="#e67e22">R</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td bgcolor="#e74c3c">I</td> <td
     * bgcolor="#e74c3c">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td>
     * <td colspan="4">法律主体码</td> <td colspan="3">站点标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td
     * colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                当前时间，必填
     * @param dataVersion        数据版本，不填，默认为1
     * @param eventCode          事件码，必
     * @param extensionCode      扩展字段: <=27位
     * @param legalInst          法律主体码
     * @param siteId             站点标识
     * @param userId             用户分表字段
     * @param userRadomPartition 用户随机位
     * @param seq                流水号
     * @return 标准流水号生成规则
     */
    public static String generateIdForAPlus(Date now, String dataVersion, String eventCode,
                                            String extensionCode, String legalInst, String siteId, String userId,
                                            String userRadomPartition, long seq) {

        if (seq < 0) {
            throw new RuntimeException("sequence不合法");
        }

        String today = getPartition(now);

        dataVersion = StringUtils.defaultIfBlank(dataVersion, DataVersion.DEFAULT);
        if (dataVersion.length() != 1) {
            throw new RuntimeException("invalid dataVersion");
        }

        if (eventCode == null || eventCode.length() != 8) {
            throw new RuntimeException("invalid eventCode");
        }

        //id最大长度不超过64
        if (extensionCode != null && extensionCode.length() > 27) {
            throw new RuntimeException("extensionCode is too long");
        }
        //扩展位默认不填就""
        extensionCode = StringUtils.defaultIfBlank(extensionCode, DEFAULT_EXTENSION_CODE);

        if (legalInst == null || legalInst.length() != 4) {
            throw new RuntimeException("invalid legalInst");
        }

        siteId = StringUtils.defaultIfBlank(siteId, DEFAULT_SITE_ID);
        if (siteId.length() != 3) {
            throw new RuntimeException("invalid siteId");
        }
        String userDBKey = getUserPartitionByUserId(userId);

        //客户端指定2位DBkey，若不指定，随机产生两位
        if (StringUtils.isBlank(userRadomPartition) || userRadomPartition.length() != 2) {
            Random random = new Random();
            userRadomPartition = StringUtils.alignRight(String.valueOf(random.nextInt(99)), 2,
                COMPLETE_STR);
        }

        //如果不满8位,左边补"0"
        String realSeq = getStandardSeq(seq, SEQUENCE_KEY_LENGTH);

        return today + dataVersion + getCurrentRegionCode() + eventCode
               + extensionCode + legalInst + siteId + userDBKey + userRadomPartition + realSeq;
    }

    private static String getCurrentRegionCode() {
        // todo
        //return AppInfo.xxx;
        return null;
    }

    private static String parseRegionCode(String relatedKey) {
        AssertUtils.notBlank(relatedKey, CommonErrorCodeEnum.ILLEGAL_PARAM);

        return StringUtils.substring(relatedKey, 9, 10);
    }


    private static boolean isValidRegionCode(String regionCode) {
        return true;
    }
}
