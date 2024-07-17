package org.shoulder.data.uid;

import org.shoulder.core.util.StringUtils;

import java.util.Date;

import static org.shoulder.data.uid.IdSpecification.adjustSequenceLength;
import static org.shoulder.data.uid.IdSpecification.extractRandomShardingFromStandardId;
import static org.shoulder.data.uid.IdSpecification.extractTenantCodeFromStandardId;
import static org.shoulder.data.uid.IdSpecification.extractUserRandomShardingFromUserIdOrDefault;
import static org.shoulder.data.uid.IdSpecification.extractUserShardingFromStandardId;
import static org.shoulder.data.uid.IdSpecification.extractUserShardingFromUserIdOrDefault;
import static org.shoulder.data.uid.IdSpecification.getCurrentRegionCode;
import static org.shoulder.data.uid.IdSpecification.standardizeBizCode;
import static org.shoulder.data.uid.IdSpecification.standardizeDataVersion;
import static org.shoulder.data.uid.IdSpecification.standardizeDate;
import static org.shoulder.data.uid.IdSpecification.standardizeEventCode;
import static org.shoulder.data.uid.IdSpecification.standardizeExtension;
import static org.shoulder.data.uid.IdSpecification.standardizeSequence;
import static org.shoulder.data.uid.IdSpecification.standardizeSharding;
import static org.shoulder.data.uid.IdSpecification.standardizeSystemCode;
import static org.shoulder.data.uid.IdSpecification.standardizeTenantCode;
import static org.shoulder.data.uid.IdSpecification.standardizeUserRandomSharding;

/**
 * 流水号生成工具（除标记位其他位都是数字）
 * <p>
 *     实际使用：通过userId/relatedTransactionId -> hashCode -> compositeVirtualSpecId, genSequence, compositeId
 * </p>
 * @see org.springframework.util.DigestUtils#md5DigestAsHex(byte[]) 随机分库分表：根据多个字段（如clientId+requestId）计算多个分库分表位，特符号链接，然后md5Hex,然后取两位并转为数字
 *
 * @author lym
 */
public class IdGenerationUtil implements IdSpecification {

    /**
     * userId
     * <p>
     * 格式：版本号(1)+租户码(3)+用户类型(1)+sequence(10)+校验码（1）
     *
     * @param dataVersion 规则版本号       当前时间，必填
     * @param tenantCode  非必填，默认是1
     * @param userType    0压测、1个人、2单位、3内部、
     * @param sequence    sequence，必填，标准长度是
     * @return id  流水号ID
     */
    public static String generateUserId(String dataVersion, String tenantCode, String userType, long sequence) {
        String uid = standardizeDataVersion(dataVersion)
                + standardizeTenantCode(tenantCode)
                + standardizeDataVersion(userType)
                + adjustSequenceLength(sequence, 9, COMPLETE_STR);
        return uid + StringUtils.computeCheckSum(uid);
    }

    /**
     * 简单ID生成：适用于非关键流水ID，如一些后台系统，配置表的主键
     * <p>
     * 格式：日期(8)+数据版本位(1)+区域位(1)+sequence(8)
     *
     * @param now         当前时间，必填
     * @param dataVersion 非必填，默认是1
     * @param seq         sequence，必填，标准长度是8位，超过8位截取后八位，不足八位前面补齐0
     * @return id  流水号ID
     */
    public static String generateId(Date now, String dataVersion, long seq) {

        return standardizeDate(now)
                + standardizeDataVersion(dataVersion)
                + getCurrentRegionCode()
                + standardizeSequence(seq);
    }

    /**
     * 简单ID生成：适用于非关键流水ID，如一些后台系统，配置表的主键
     * <p>
     * 格式：日期(8)+数据版本位(1)+区域位(1)+分表位(2)+sequence(7)
     *
     * @param now   当前时间，必填
     * @param split 分表位【00~99】必填，不足两位前面补0
     * @param seq   sequence，必填，此处长度是7位，超过7位截取后7位，不足7位前面补齐0
     * @return id  流水号ID
     */
    public static Long generateIdInNumber(Date now, String split, long seq) {

        String realSeq = adjustSequenceLength(seq, 7, COMPLETE_STR);

        return Long.parseLong(standardizeDate(now) + getCurrentRegionCode() + standardizeSharding(split) + realSeq);
    }

    /**
     * 简单ID生成：适用于非关键流水ID，如一些后台系统，配置表的主键
     * <p>
     * 格式：日期(8)+数据版本位(1)+区域位(1)+租户码(3)+sequence(8)
     *
     * @param now         当前时间，必填
     * @param dataVersion 非必填，默认是1
     * @param tenantCode  必填
     * @param seq         sequence，必填，标准长度是8位，超过8位截取后八位，不足八位前面补齐0
     * @return id  流水号ID
     */
    public static String generateId(Date now, String dataVersion, String tenantCode, long seq) {

        return standardizeDate(now)
                + standardizeDataVersion(dataVersion)
                + getCurrentRegionCode()
                + standardizeTenantCode(tenantCode)
                + standardizeSequence(seq);
    }

    /**
     * <li>所有流水生成必须使用该方法<br>
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">S</td> <td bgcolor="#ddccbb">S</td> <td
     * bgcolor="#ddccbb">S</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td
     * >X...</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填默认1
     * @param systemCode          系统标志码，必填
     * @param bizCode             业务标志码，不填默认000
     * @param extensionCode       扩展码，长度小于等于32位,可不填
     * @param accountNo           账号或用户ID
     * @param userRandomPartition 用户随机分表位,若不指定，随机产生两位，一般用于流水号FO
     * @param seq                 序列号
     * @return 流水号ID
     */
    public static String generateId(Date now, String dataVersion, String systemCode,
                                    String bizCode, String extensionCode, String accountNo,
                                    String userRandomPartition, long seq) {

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeSystemCode(systemCode)
                + standardizeBizCode(bizCode)
                + standardizeExtension(extensionCode, 32)
                + extractUserShardingFromUserIdOrDefault(accountNo)
                + standardizeUserRandomSharding(userRandomPartition)
                + standardizeSequence(seq);
    }

    /**
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">S</td> <td bgcolor="#ddccbb">S</td> <td
     * bgcolor="#ddccbb">S</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td
     * >X...</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填默认1
     * @param systemCode          系统标志码，必填
     * @param bizCode             业务标志码，不填默认空
     * @param eventCode           业务事件码，不填默认空
     * @param extensionCode       扩展码，长度小于等于32位,可不填
     * @param tenantCode          租户码
     * @param accountNo           账号或用户ID
     * @param userRandomPartition 用户随机分表位,若不指定，随机产生两位，一般用于流水号FO
     * @param seq                 序列号
     * @return 流水号ID
     */
    public static String generateId(Date now, String dataVersion, String systemCode,
                                    String bizCode, String eventCode, String extensionCode, String tenantCode,
                                    String accountNo, String userRandomPartition, long seq) {

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeSystemCode(systemCode) + standardizeBizCode(bizCode)
                + standardizeEventCode(eventCode)
                + standardizeExtension(extensionCode, 32) + standardizeTenantCode(tenantCode)
                + extractUserShardingFromUserIdOrDefault(accountNo)
                + standardizeUserRandomSharding(userRandomPartition)
                + standardizeSequence(seq);
    }

    /**
     * 根据已经生成的关联流水号，生成同样分库分表的新流水号
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">S</td> <td bgcolor="#ddccbb">S</td> <td
     * bgcolor="#ddccbb">S</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td
     * >X...</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
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
     * @param standardId    业务关联key
     * @param seq           序列号
     * @return 流水号ID
     */
    public static String generateIdByRelatedKey(Date now, String dataVersion, String systemCode,
                                                String bizCode, String extensionCode,
                                                String standardId, long seq) {

        String dbPrimaryKey = generateId(now, dataVersion, systemCode, bizCode, extensionCode,
                null, null, seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START)
                + extractUserShardingFromStandardId(standardId)
                + standardizeUserRandomSharding(extractRandomShardingFromStandardId(standardId))
                + StringUtils.substring(dbPrimaryKey, RANDOM_USER_DB_KEY_END);
    }


    /**
     * 无用户id，并指定分库分表，生成新的流水ID
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">S</td> <td bgcolor="#ddccbb">S</td> <td
     * bgcolor="#ddccbb">S</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td bgcolor="#11eeee">B</td> <td
     * >X...</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="3">系统标识码</td> <td colspan="3">业务标识码</td> <td
     * colspan="1">扩展位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填默认1
     * @param systemCode          系统标志码，必填
     * @param bizCode             业务标志码，不填默认000
     * @param extensionCode       扩展码，长度小于等于32位，可不填
     * @param partitionKey        指定用户分库
     * @param userRandomPartition 指定用户分库随机位
     * @param seq                 序列号
     * @return 流水号ID
     */
    public static String generateIdByPartitionKey(Date now, String dataVersion, String systemCode,
                                                  String bizCode, String extensionCode,
                                                  String partitionKey, String userRandomPartition,
                                                  long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateId(now, dataVersion, systemCode, bizCode, extensionCode,
                null, userRandomPartition, seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
                + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    // todo 最常用

    /**
     * 生成35位ID
     * <li> 新增租户、业务事件码
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td>X...</td> <td bgcolor="#11eeee">T</td> <td bgcolor="#11eeee">T</td> <td
     * bgcolor="#11eeee">T</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">租户标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填，默认为1
     * @param eventCode           事件码，必
     * @param extensionCode       扩展字段: <=32位
     * @param tenantCode          租户标识
     * @param userId              用户分表字段
     * @param userRandomPartition 用户随机位
     * @param seq                 流水号
     * @return 标准流水号生成规则
     */
    public static String generateStandardId(Date now, String dataVersion, String eventCode,
                                            String extensionCode, String tenantCode, String userId,
                                            String userRandomPartition, long seq) {

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeEventCode(eventCode)
                + standardizeExtension(extensionCode, 32)
                + standardizeTenantCode(tenantCode)
                + extractUserShardingFromUserIdOrDefault(userId)
                + standardizeUserRandomSharding(userRandomPartition)
                + standardizeSequence(seq);
    }

    /**
     * 生成ID,standardId需要符合35位标准
     *
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td>X...</td> <td bgcolor="#11eeee">T</td> <td bgcolor="#11eeee">T</td> <td
     * bgcolor="#11eeee">T</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">租户标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">分库位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now           当前时间
     * @param dataVersion   数据版本: 1位
     * @param eventCode     事件码: 8位
     * @param extensionCode 扩展字段: <=32位
     * @param standardId    关联流水号
     * @param seq           流水号
     * @return 标准流水号
     */
    public static String generateStandardIdWithRelateKey(Date now, String dataVersion,
                                                         String eventCode, String extensionCode,
                                                         String standardId, long seq) {

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeEventCode(eventCode)
                + standardizeExtension(extensionCode, 32)
                + extractTenantCodeFromStandardId(standardId)
                + extractUserShardingFromStandardId(standardId)
                + standardizeUserRandomSharding(extractRandomShardingFromStandardId(standardId))
                + standardizeSequence(seq);
    }

    /**
     * 生成35位ID
     * <li> 新增租户、业务事件码
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td>X...</td> <td bgcolor="#11eeee">T</td> <td bgcolor="#11eeee">T</td> <td
     * bgcolor="#11eeee">T</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">租户标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填，默认为1
     * @param eventCode           事件码，必
     * @param extensionCode       扩展字段: <=32位
     * @param tenantCode          租户标识
     * @param partitionKey        用户指定分库分表字段
     * @param userRandomPartition 用户随机位
     * @param seq                 流水号
     * @return 标准流水号生成规则
     */
    public static String generateStandardIdWithPartitionKey(Date now, String dataVersion, String eventCode,
                                                            String extensionCode, String tenantCode, String partitionKey,
                                                            String userRandomPartition, long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateStandardId(now, dataVersion, eventCode, extensionCode, tenantCode, null, userRandomPartition, seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
                + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    /**
     * 生成ID，分库分表位是userID倒数2、3位，用户随机分表位是倒数4、5位
     *
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td>X...</td> <td bgcolor="#11eeee">T</td> <td bgcolor="#11eeee">T</td> <td
     * bgcolor="#11eeee">T</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td
     * bgcolor="#eecc88">U</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td> <td
     * colspan="3">租户标示位</td> <td colspan="4">客户分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now           当前时间
     * @param dataVersion   数据版本: 1位
     * @param eventCode     事件码: 8位
     * @param extensionCode 扩展字段: <=32位
     * @param tenantCode    租户标识
     * @param userId        用户分表字段
     * @param seq           流水号
     * @return id
     */
    public static String generateIdWithNoRandomUserRule(Date now, String dataVersion,
                                                        String eventCode, String extensionCode,
                                                        String tenantCode, String userId, long seq) {

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeEventCode(eventCode)
                + standardizeExtension(extensionCode, 32)
                + standardizeTenantCode(tenantCode)
                + extractUserRandomShardingFromUserIdOrDefault(userId)
                + standardizeSequence(seq);
    }

    /**
     * 生成，支持主体隔离的37位ID
     *
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td>X...</td>
     *
     * <td bgcolor="#8080C0">L</td> <td bgcolor="#8080C0">L</td>
     * <td bgcolor="#8080C0">L</td> <td bgcolor="#8080C0">L</td>
     *
     * <td bgcolor="#11eeee">S</td> <td bgcolor="#11eeee">S</td>
     * <td
     * bgcolor="#11eeee">S</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td>
     * <td colspan="4">法律主体码</td>
     * <td colspan="3">租户标示位</td>
     * <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填，默认为1
     * @param eventCode           事件码，必
     * @param extensionCode       扩展字段: <=27位
     * @param legalInst           法律主体码
     * @param tenantCode          租户标识
     * @param partitionKey        用户指定分库分表字段
     * @param userRandomPartition 用户随机位
     * @param seq                 流水号
     * @return 流水号ID
     */
    public static String generateStandardIdWithPartitionKey(Date now, String dataVersion, String eventCode,
                                                            String extensionCode, String legalInst, String tenantCode, String partitionKey,
                                                            String userRandomPartition, long seq) {

        if (StringUtils.isBlank(partitionKey) || partitionKey.length() != 2) {
            throw new RuntimeException("partitionKey is invalid");
        }

        String dbPrimaryKey = generateStandardId(now, dataVersion, eventCode, extensionCode, legalInst, tenantCode, null, userRandomPartition,
                seq);

        return StringUtils.substring(dbPrimaryKey, 0, USER_DB_KEY_START) + partitionKey
                + StringUtils.substring(dbPrimaryKey, USER_DB_KEY_END);
    }

    /**
     * 生成，支持主体隔离的37位ID
     *
     * <table border="1" style="border-color: #bbccdd">
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
     * <td></td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td bgcolor="#000000">D</td> <td
     * bgcolor="#bbcccc">V</td> <td bgcolor="#bbccdd">R</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td
     * bgcolor="#ddccbb">E</td> <td bgcolor="#ddccbb">E</td> <td></td>
     *
     * <td bgcolor="#11eeee">L</td> <td bgcolor="#11eeee">L</td> <td bgcolor="#11eeee">L</td> <td bgcolor="#11eeee">L</td>
     * <td bgcolor="#11eeee">S</td> <td bgcolor="#11eeee">S</td> <td bgcolor="#11eeee">S</td>
     * <td bgcolor="#eecc88">U</td> <td bgcolor="#eecc88">U</td> <td bgcolor="#dddd99">R</td> <td
     * bgcolor="#dddd99">R</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td bgcolor="#dd9988">I</td> <td
     * bgcolor="#dd9988">I</td>
     * </tr>
     * <tr>
     * <td>说明</td> <td colspan="8">8位日期</td> <td>数据版本</td> <td>区域位</td> <td colspan="8">业务事件码</td> <td>扩展位</td>
     * <td colspan="4">法律主体码</td> <td colspan="3">租户标示位</td> <td colspan="2">客户分表位</td> <td colspan="2">随机分表位</td> <td
     * colspan="8">Sequence空间</td>
     * </tr>
     * </table>
     *
     * @param now                 当前时间，必填
     * @param dataVersion         数据版本，不填，默认为1
     * @param eventCode           事件码，必
     * @param extensionCode       扩展字段: <=27位
     * @param legalInst           法律主体码
     * @param tenantCode          租户标识
     * @param userId              用户分表字段
     * @param userRandomPartition 用户随机位
     * @param seq                 流水号
     * @return 标准流水号生成规则
     */
    public static String generateStandardId(Date now, String dataVersion, String eventCode,
                                            String extensionCode, String legalInst, String tenantCode, String userId,
                                            String userRandomPartition, long seq) {

        if (legalInst == null || legalInst.length() != 4) {
            throw new RuntimeException("invalid legalInst");
        }

        return standardizeDate(now) + standardizeDataVersion(dataVersion) + getCurrentRegionCode()
                + standardizeEventCode(eventCode)
                + standardizeExtension(extensionCode, 27)
                + legalInst + standardizeTenantCode(tenantCode)
                + extractUserShardingFromUserIdOrDefault(userId)
                + standardizeUserRandomSharding(userRandomPartition)
                + standardizeSequence(seq);
    }

}
