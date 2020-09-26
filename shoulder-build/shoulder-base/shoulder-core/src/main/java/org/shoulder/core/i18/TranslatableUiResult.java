package org.shoulder.core.i18;

import org.shoulder.core.dto.response.RestResult;

import java.util.List;

/**
 * 可翻译的接口返回值（一般用于给前端，{@link RestResult} 定位为 api 接口返回值，故未继承该接口）
 *
 * @author lym
 */
public interface TranslatableUiResult<T> {

    /**
     * code @return code
     */
    String getCode();

    /**
     * data @return data
     */
    T getData();

    /**
     * msg @return msg
     */
    String getMsg();

    /**
     * 多语言翻译时的用于填充占位符，如果没有占位符则自动返回空（代表多语言字段无可填充项，直接翻译即可）
     *
     * @return 填充翻译的部分
     */
    default List<String> getPlaceholders() {
        return null;
    }

    /**
     * 统一格式转换：转为框架标准的
     *
     * @return 框架中提供的
     */
    default RestResult<T> unified() {
        return new RestResult<>(getCode(), getMsg(), getData());
    }
}
