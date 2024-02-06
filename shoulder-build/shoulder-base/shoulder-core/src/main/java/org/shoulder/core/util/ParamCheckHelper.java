package org.shoulder.core.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 参数检查类.
 * 当出现特定的参数校验错误时,给出特定格式的返回信息。
 *
 * @author lym
 */
public class ParamCheckHelper {

    /** 入参字符串不为空时的错误描述 */
    public static final String STRING_NOT_BLANK = "The value of %s must be blank.";

    /** 入参字符串为空时的错误描述 */
    public static final String STRING_IS_BLANK = "The value of %s can not be blank.";

    /** 入参字符串同时为空的错误描述 */
    public static final String STRING_BOTH_BLANK = "The value of %s and %s can not be blank at the same time.";

    /** 入参字符串集合中包含空元素时的错误描述 */
    public static final String CONTAINS_BLANK_ELEMENT = "The set of %s contains blank element.";

    /** 入参字符串集合中包含重复字符串时的错误描述 */
    public static final String CONTAINS_DUPLICATE_STRING = "The set of %s contains duplicate string";

    /** 入参对象为空时的错误描述 */
    public static final String OBJECT_IS_NULL = "The value of %s can not be null.";

    /** 入参未定义或者不存在时的错误描述 */
    public static final String VALUE_IS_UNDEFINED = "The value of %s must exist or is pre-defined.";

    /** 入参不匹配时的错误描述 */
    public static final String VALUE_IS_UNEXPECTED = "The value of %s is not expected.";

    /** 入参整数小于等于零时的错误描述 */
    public static final String NUMBER_NOT_GREATER_THAN_ZERO = "The value of %s must be greater than zero.";

    /** 入参超过上限值时的错误描述 */
    public static final String INTEGER_EXCEED_UPPER_LIMIT = "The value of %s exceeds the maximum value.";

    /** 入参格式不合法时的错误描述 */
    public static final String UNEXPECTED_FORMAT = "The format of %s is not the expected format.";

    /** 入参param1与param2不相等时的错误描述 */
    public static final String STRING_NOT_EQUAL = "The value of %s must equal to the value of %s.";

    /** 入参param1与param2相等时的错误描述 */
    public static final String STRING_EQUAL = "The value of %s must not equal to the value of %s.";

    /** 条件入参有值时指定入参不能为空 */
    public static final String STRING_IS_BLANK_WHEN_ANOTHER_PROVIDED = "The value of %s can not be blank if %s is provided.";

    /** 条件入参为空时,指定入参必需为空 */
    public static final String STRING_NOT_BLANK_WHEN_ANOTHER_BLANK = "The value of %s must be blank if %s is not provided.";

    /** 集合大小超过上限时的错误描述 */
    public static final String SET_SIZE_EXCEED_LIMIT = "The set of %s exceeds the value of interval.";

    /** 入参字符串长度超过限制的错误描述 */
    public static final String STRING_LENGTH_EXCEED_LIMIT = "The length of %s exceeds the value of interval.";

    /** 集合为空时的错误描述 */
    public static final String SET_IS_EMPTY = "The set of %s can not be empty.";

    /** 入参布尔1与布尔2相等时的错误描述 */
    public static final String BOOLEAN_EQUAL = "The value of %s not equal to the value of %s.";

    /** 入参对象类型不匹配 */
    public static final String OBJECT_TYPE_UNEXPECTED = "the type of %s must be %s type.";

    /** 入参字符串不符合Json格式 */
    public static final String STRING_NOT_JSON_FORMAT = "the value of %s is not json format.";

    /** 入参日期不大于或小于期望的值 */
    public static final String DATE_NOT_GREATER_OR_LESS_THAN_VALUE = "the value of %s not %s the value of %s.";

    /** 入参的值不大于等于或小于等于期望的边界值 */
    public static final String NUMBER_NOT_IN_INTERVAL = "the value of %s must greater equal %s and less equal %s.";

    /** 入参日期格式不是期望的 */
    public static final String DATE_FORMAT_UNEXPECTED = "the date format of %s is not the expected.";

    /** 无明确错误原因的参数异常描述 */
    public static final String VALUE_IS_ILLEGAL = "the value of %s is illegal.";

    /** 多个参数值同时为空 */
    public static final String VALUE_SAME_BLANK = "the value of %s can not be blank at the same time.";

    /** 合法角色ID长度 */
    private static final int VALID_ROLE_ID_LENGTH = 21;

    /** 入参金钱类中的金额属性值不为数字 */
    public static final String VALUE_NOT_NUMBER = "the value of %s must be number.";

    /** 入参金额小于等于零时的错误描述 */
    public static final String AMOUNT_NOT_GREATER_THAN_ZERO = "The amount of %s must be greater than zero.";

    /** 入参金额小于零时的错误描述 */
    public static final String AMOUNT_NOT_GREATER_THAN_OR_EQUAL_TO_ZERO = "The amount of %s must be greater than or equal to zero.";

    /** 入参金额同有大于等于期望值 */
    public static final String AMOUNT_NOT_GREATER_EQUAL_THAN_VALUE = "the amount of %s must greater equal than %s.";

    /** 入参金额没有大于期望值 */
    public static final String AMOUNT_NOT_GREATER_THAN_VALUE = "the amount of %s must greater than %s.";

    /** 入参金额没有小于等于期望值 */
    public static final String AMOUNT_NOT_LESS_EQUAL_THAN_VALUE = "the amount of %s must less equal than %s.";

    /** 入参金额没有小于期望值 */
    public static final String AMOUNT_NOT_LESS_THAN_VALUE = "the amount of %s must less than %s.";

    /**
     * 验证入参字符串必须为空字符串
     *
     * @param param      请求参数
     * @param paramName  参数名称
     * @param resultCode 错误码
     */
    public static void blank(String param, String paramName, BaseRuntimeException resultCode) {
        AssertUtils.blank(param, resultCode, String.format(STRING_NOT_BLANK, paramName));
    }

    /**
     * 验证入参字符串不为空字符串
     *
     * @param param      请求参数字符串
     * @param paramName  参数名称
     * @param resultCode 错误码
     */
    public static void notBlank(String param, String paramName, BaseRuntimeException resultCode) {
        AssertUtils.notBlank(param, resultCode, String.format(STRING_IS_BLANK, paramName));
    }

    /**
     * 验证请求参数不能同时为空
     *
     * @param param1        请求参数1
     * @param paramName1    请求参数1名称
     * @param param2        请求参数2
     * @param paramName2    请求参数2名称
     * @param resultCode    错误码
     */
    public static void notEitherBlank(Object param1, String paramName1, Object param2,
                                      String paramName2, BaseRuntimeException resultCode) {
        boolean isParam1Blank = isBlankParam(param1);
        boolean isParam2Blank = isBlankParam(param2);

        AssertUtils.isTrue(!isParam1Blank || !isParam2Blank, resultCode,
            String.format(STRING_BOTH_BLANK, paramName1, paramName2));
    }

    /**
     * 参数是否为空
     * 字符串时判断是否为空内容,即StringUtils.isBlank; 对象时判断是否为null
     *
     * @param param 请求参数
     * @return 是否为空
     */
    private static boolean isBlankParam(Object param) {
        if (param == null) {
            return true;
        }

        if (param instanceof String) {
            return StringUtils.isBlank((String) param);
        }

        return false;
    }

    /**
     * 验证入参集合不包含空元素。 如果元素为字符串类型, 则要求不为空字符串; 否则要求元素为非null对象。
     * <strong>如果集合为空集合, 认为是不包含空元素的的, 故不会抛出参数错误。</strong>
     *
     * @param param         入参集合
     * @param paramName     入参集合名称
     * @param resultCode    错误码
     */
    public static void notContainsBlank(List<?> param, String paramName,
                                        BaseRuntimeException resultCode) {
        if (CollectionUtils.isEmpty(param)) {
            return;
        }

        for (Object obj : param) {
            if (obj instanceof String) {
                AssertUtils.notBlank((String) obj, resultCode,
                    String.format(CONTAINS_BLANK_ELEMENT, paramName));

            } else {
                AssertUtils.notNull(obj, resultCode,
                    String.format(CONTAINS_BLANK_ELEMENT, paramName));
            }
        }
    }

    /**
     * 验证入参字符串集合不包含重复字符串元素
     * <strong>如果字符串集合为空集合, 认为是不包含重复元素的, 故不会抛出参数错误。</strong>
     *
     * @param param         字符串集合
     * @param paramName     字符串集合名称
     * @param resultCode    错误码
     */
    public static void unduplicated(List<String> param, String paramName,
                                    BaseRuntimeException resultCode) {
        if (CollectionUtils.isEmpty(param)) {
            return;
        }

        AssertUtils.isTrue(param.size() == new HashSet<>(param).size(), resultCode,
            String.format(CONTAINS_DUPLICATE_STRING, paramName));
    }

    /**
     * 当条件属性(anotherParam)有值时, 指定入参(param)不能为空。
     *
     * @param param             待校验的入参字符串
     * @param paramName         待校验入参字符串名称
     * @param anotherParam      是否有值的条件字符串
     * @param anotherParamName  是否有值的条件字符串名称
     * @param resultCode        错误码
     */
    public static void notBlankWhenAnotherNotBlank(String param, String paramName,
                                                   String anotherParam, String anotherParamName,
                                                   BaseRuntimeException resultCode) {
        if (StringUtils.isNotBlank(anotherParam)) {
            AssertUtils.notBlank(param, resultCode,
                String.format(STRING_IS_BLANK_WHEN_ANOTHER_PROVIDED, paramName, anotherParamName));
        }
    }

    /**
     * 当条件属性(anotherParam)为空时, 指定入参(param)必须为空。
     *
     * @param param             待校验的入参字符串
     * @param paramName         待校验入参字符串名称
     * @param anotherParam      是否有值的条件字符串
     * @param anotherParamName  是否有值的条件字符串名称
     * @param resultCode        错误码
     */
    public static void blankWhenAnotherBlank(String param, String paramName, String anotherParam,
                                             String anotherParamName,
                                             BaseRuntimeException resultCode) {
        if (StringUtils.isBlank(anotherParam)) {
            AssertUtils.blank(param, resultCode,
                String.format(STRING_NOT_BLANK_WHEN_ANOTHER_BLANK, paramName, anotherParamName));
        }
    }

    /**
     * 验证入参对象不能为空
     *
     * @param param      请求参数对象
     * @param paramName  参数名称
     * @param resultCode 错误码
     */
    public static void notNull(Object param, String paramName, BaseRuntimeException resultCode) {
        AssertUtils.notNull(param, resultCode, String.format(OBJECT_IS_NULL, paramName));
    }

    /**
     * 传入值与约定值不一致,包括与枚举类中定义不一致,参数中心并没有定义导致与约定不一致的
     * <strong>注意强调的是没有定义 </strong>
     *
     * @param isParamDefined  请求参数是否有定义
     * @param paramName       参数名称
     * @param resultCode      错误码
     */
    public static void isDefined(boolean isParamDefined, String paramName,
                                 BaseRuntimeException resultCode) {
        AssertUtils.isTrue(isParamDefined, resultCode, String.format(VALUE_IS_UNDEFINED, paramName));
    }

    /**
     * 传入值在枚举类或者是参数中心,但是该业务场景下必须是指定的枚举值
     * <strong>注意强调的是不区配</strong>
     *
     * @param isParamExpected   请求入参值是否符合期望
     * @param paramName         参数名称
     * @param resultCode        错误码
     */
    public static void isExpected(boolean isParamExpected, String paramName,
                                  BaseRuntimeException resultCode) {
        AssertUtils.isTrue(isParamExpected, resultCode,
            String.format(VALUE_IS_UNEXPECTED, paramName));
    }

    /**
     * 指定集合中包含入参元素, 判断入参元素是否期望的数值。
     *
     * @param param                 入参对象
     * @param paramName             入参对象名称
     * @param expectedCollection    期望元素集合
     * @param resultCode            错误码
     */
    public static void isExpected(Object param, String paramName, Collection<?> expectedCollection,
                                  BaseRuntimeException resultCode) {

        AssertUtils.contains(expectedCollection, param, resultCode,
            String.format(VALUE_IS_UNEXPECTED, paramName));
    }

    /**
     * 传入的参数必须大于零，如分页的条数或者是页数，如果要求必须大于零的
     *
     * @param param       请求参数
     * @param paramName   请求参数名称
     * @param resultCode  错误码
     */
    public static void greaterThanZero(int param, String paramName, BaseRuntimeException resultCode) {
        AssertUtils.isTrue(param > 0, resultCode,
            String.format(NUMBER_NOT_GREATER_THAN_ZERO, paramName));
    }

    /**
     * 传入的参数必须小于等于上限值，如好友的数量，每页最大的条数
     *
     * @param value      请求入参
     * @param paramName  参数名称
     * @param upperLimit 参数上限值
     * @param resultCode 错误码
     */
    public static void notExceedLimit(int value, String paramName, int upperLimit,
                                      BaseRuntimeException resultCode) {
        AssertUtils.isTrue(value <= upperLimit, resultCode,
            String.format(INTEGER_EXCEED_UPPER_LIMIT, paramName));
    }

    /**
     * 传入参数按照指定格式
     * <strong>注意强调的是格式</strong>
     *
     * @param isParamExceptedFormat 入参是否期望格式
     * @param paramName             参数名称
     * @param resultCode            错误码
     */
    public static void isExpectedFormat(boolean isParamExceptedFormat, String paramName,
                                        BaseRuntimeException resultCode) {
        AssertUtils.isTrue(isParamExceptedFormat, resultCode,
            String.format(UNEXPECTED_FORMAT, paramName));
    }

    /**
     * 入参字符串1与入参字符串2必须相等
     *
     * @param param1        入参字符串1值
     * @param paramName1    入参字符串1名称
     * @param param2        入参字符串2值
     * @param paramName2    入参字符串2名称
     * @param resultCode    错误码
     */
    public static void equals(String param1, String paramName1, String param2, String paramName2,
                              BaseRuntimeException resultCode) {
        AssertUtils.isTrue(StringUtils.equals(param1, param2), resultCode,
            String.format(STRING_NOT_EQUAL, paramName1, paramName2));
    }

    /**
     * 入参字符串1与入参字符串2必须不相等
     *
     * @param param1        入参字符串1值
     * @param paramName1    入参字符串1名称
     * @param param2        入参字符串2值
     * @param paramName2    入参字符串2名称
     * @param resultCode    错误码
     */
    public static void notEqual(String param1, String paramName1, String param2, String paramName2,
                                BaseRuntimeException resultCode) {
        AssertUtils.isFalse(StringUtils.equals(param1, param2), resultCode,
            String.format(STRING_EQUAL, paramName1, paramName2));
    }

    /**
     * 入参布尔1与入参布尔2必须不相等
     *
     * @param param1        入参布尔1值
     * @param paramName1    入参布尔1名称
     * @param param2        入参布尔2值
     * @param paramName2    入参布尔2名称
     * @param resultCode    错误码
     */
    public static void notEqual(boolean param1, String paramName1, boolean param2,
                                String paramName2, BaseRuntimeException resultCode) {
        AssertUtils.notEquals(param1, param2, resultCode,
            String.format(BOOLEAN_EQUAL, paramName1, paramName2));
    }

    /**
     * 集合不可以为空或者集合大小为零
     *
     * @param param      请求入参集合
     * @param paramName  参数名称
     * @param resultCode 错误码
     */
    public static void notEmpty(Collection<?> param, String paramName,
                                BaseRuntimeException resultCode) {
        AssertUtils.notEmpty(param, resultCode, String.format(SET_IS_EMPTY, paramName));
    }

    /**
     * 集合的大小不能超过数量上限
     *
     * @param param      请求入参集合
     * @param paramName  请求入参集合名称
     * @param sizeLimit  集合大小上限
     * @param resultCode 错误码
     */
    public static void notExceedSizeLimit(Collection<?> param, String paramName, int sizeLimit,
                                          BaseRuntimeException resultCode) {
        if (!CollectionUtils.isEmpty(param)) {
            AssertUtils.isTrue(param.size() <= sizeLimit, resultCode,
                String.format(SET_SIZE_EXCEED_LIMIT, paramName));
        }
    }

    /**
     * 入参字符串不能超出长度限制
     *
     * @param param         入参字符串
     * @param paramName     入参字符串名称
     * @param lengthLimit   长度限制
     * @param resultCode    结果码
     */
    public static void notExceedLengthLimit(String param, String paramName, int lengthLimit,
                                            BaseRuntimeException resultCode) {
        if (StringUtils.isNotBlank(param)) {
            AssertUtils.isTrue(param.length() <= lengthLimit, resultCode,
                String.format(STRING_LENGTH_EXCEED_LIMIT, paramName));
        }
    }

    /**
     * 入参字符串是合法的角色ID
     *
     * @param param         入参字符串
     * @param paramName     入参字符串名称
     * @param resultCode    结果码
     */
    public static void isValidRoleId(String param, String paramName, BaseRuntimeException resultCode) {
        AssertUtils.isTrue(NumberUtils.isDigits(param), resultCode,
            String.format(VALUE_IS_ILLEGAL, paramName));
        AssertUtils.isTrue(param.length() == VALID_ROLE_ID_LENGTH, resultCode,
            String.format(VALUE_IS_ILLEGAL, paramName));
    }

}
