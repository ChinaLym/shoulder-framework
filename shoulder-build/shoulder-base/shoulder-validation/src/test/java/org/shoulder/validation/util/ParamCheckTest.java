package org.shoulder.validation.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.ExceptionUtil;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.shoulder.validate.util.ParamCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tip info test
 *
 * @author lym
 */
public class ParamCheckTest {

    private static final String testKeyName = "testKey";

    @Test
    public void testTipInfo() {
        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notNull(null, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notEmpty("", testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notEmpty(new Object[0], testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notEmpty(new ArrayList<>(), testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notEmpty(new HashMap<>(), testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notBlank("", testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertDoesNotThrow(() -> ParamCheck.noNullElements(new ArrayList<>(), testKeyName));
        List<String> list = new ArrayList<>();
        list.add(null);
        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.noNullElements(list, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_BLANK.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.notContain("abc", "ab", testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.eGreater(1, 4, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_OUT_RANGE.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.greater(1, 4, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_OUT_RANGE.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.assertBetween(1, 4, 5, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_OUT_RANGE.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.isNull("1", testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.sizeLimit(-1, 20, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.sizeLimit(25, 20, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.sizeLimit(List.of(25, 26), 0, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.assertIn(1, List.of(25, 26), testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage(), testKeyName)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.isInstanceOf(1, String.class, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH.getMessage(), testKeyName, Integer.class.getName(), String.class)
        );

        Assertions.assertEquals(
            Assertions.assertThrowsExactly(BaseRuntimeException.class, () -> ParamCheck.isAssignable(Integer.class, String.class, testKeyName)).getMessage(),
            ExceptionUtil.generateExceptionMessage(ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH.getMessage(), testKeyName, Integer.class.getName(), String.class)
        );

    }

}
