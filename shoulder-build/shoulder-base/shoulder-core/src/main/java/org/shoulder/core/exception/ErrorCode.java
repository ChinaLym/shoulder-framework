package org.shoulder.core.exception;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.core.i18.Translatable;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;


/**
 * 表示错误的接口
 * 必带错误码，可带提示信息的异常/错误
 * 通常错误码枚举、自定义异常类实现该接口
 * <p>
 * 小提示：业务中可以按模块或按业务定义枚举 保存错误码、错误提示信息
 * 详细的错误码规范见 <a href="https://spec.itlym.cn/specs/base/errorCode.html">错误码规范<a/>
 * 也可考虑前缀区分来源，调用方出错（如参数）/ 系统内部错误（如取缓存时反序列失败） / 第三方服务失败（如调用其他服务，结果其他服务挂了，返回500）
 * 当且shoulder 采用的以模块code区分，不够明显，不统一
 * <p>
 * 区分业务失败、业务状态未知
 *      <br><b>新版本：</b></br>
 *      <table border="1">
 *           <tr>
 *           <td><b>位置</b></td><td bgcolor="yellow">1</td><td bgcolor="yellow">2</td><td bgcolor="white">4</td><td bgcolor="red">4</td><td bgcolor="blue">5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td><td>15</td><td>16</td>
 *          </tr>
 *          <tr>
 *           <td><b>示例</b></td><td>*</td><td>*</td><td>0</td><td>1</td><td>0</td><td>1</td><td>0</td><td>1</td><td>1</td><td>0</td><td>2</td><td>7</td><td>7</td><td>7</td><td>7</td><td>7</td>
 *          </tr>
 *          <tr>
 *           <td><b>说明</b></td><td bgcolor="yellow" colspan=2>品牌<br>标识</td><td bgcolor="white">错<br>误<br>码<br>版<br>本</td><td bgcolor="red">错<br>误<br>级<br>别</td><td bgcolor="blue">错<br>误<br>类<br>型</td><td colspan=8>错误场景(8位)</td><td colspan=3>错误编<br>码(3位)</td>
 *          </tr>
 *      </table>
 *      错误级别：INFO/WARN/ERROR/FATAL
 *      错误类型：SYS / BIZ / THIRD
 *
 * todo 支持错误码注册和查询
 * @author lym
 */
public interface ErrorCode extends Translatable {

    /* ---------------------- 默认值 ------------------- */

    /**
     * 默认错误码、异常类日志记录级别为 warn
     */
    Level DEFAULT_LOG_LEVEL = Level.ERROR;

    /**
     * 默认错误码、异常类 HTTP 响应码为 500
     */
    HttpStatus DEFAULT_HTTP_STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * 特殊值，0代表成功
     */
    SuccessCode SUCCESS = new SuccessCode();

    /**
     * 特殊值，0代表成功
     */
    String SUCCESS_CODE = "0";

    /**
     * 特殊值，幂等成功
     */
    SuccessCode IDEMPOTENT_SUCCESS = new IdempotentSuccessCode();


    /* ---------------------- 方法 ------------------- */

    /**
     * 获取完整错误码
     * long → String
     * <code>String.format("0x%08x", code)<code/>
     * 或
     * <code>
     * String hex = Long.toHexString(code);
     * this.code = AppInfo.errorCodePrefix() + hex;
     * <code/>
     *
     * @return 错误码
     */
    @Nonnull
    String getCode();

    /**
     * 获取错误信息【非该错误码对应的翻译，而是错误提示，用于日志输出】
     *
     * @return 错误信息，支持 %s、{} 这种带占位符的消息
     */
    String getMessage();

    // --------------------------- 【用于支持多语言】 -----------------------------

    /**
     * 错误码对应的翻译
     *
     * @return 错误码对应的翻译
     */
    @Override
    default String getI18nKey() {
        return "err." + getCode() + ".desc";
    }

    /**
     * 补充翻译需要参数
     *
     * @return 翻译需要参数
     */
    @Override
    @Nullable
    default Object[] getArguments() {
        return getArgs();
    }

    /**
     * 当翻译失败时，返回什么
     *
     * @return msg
     */
    @Override
    @Nullable
    default String getDefaultMessage() {
        return getMessage();
    }


    // --------------------------- 【便于全局异常统一处理的方法】 -----------------------------

    /**
     * 获取用于填充错误信息的数据 【便于全局异常统一处理，非必需】
     *
     * @return 用于填充错误信息的数据
     */
    default Object[] getArgs() {
        return new Object[0];
    }

    /**
     * 设置用于填充错误信息的数据 【便于全局异常统一处理，非必需】
     *
     * @param args 用于填充错误信息的数据
     */
    /*default void setArgs(Object... args) {
        throw new UnsupportedOperationException("not support set args");
    }
*/

    /**
     * 发生该错误时用什么级别记录日志【便于全局异常统一处理，非必需】
     * 用户 / 开放接口易触发的错误定为 INFO，如填写参数错误
     * 故意 / 无意未遵守交互约定、外部依赖偶现失败、的记录 WARN
     * 核心业务失败，ERROR
     * 强依赖的服务连接失败 FATAL
     * 详细的日志级别建议见 <a href="https://spec.itlym.cn/specs/base/log.html">软件开发日志规范<a/> - 日志级别规范
     *
     * @return 日志级别
     */
    @Nonnull
    default Level getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    /**
     * 若接口中抛出该错误，返回调用方什么状态码，默认 500 【便于全局异常统一处理，非必需】
     *
     * @return httpStatusCode
     * @deprecated shoulder 默认全都使用 200
     */
    @Nonnull
    default HttpStatus getHttpStatusCode() {
        return DEFAULT_HTTP_STATUS_CODE;
    }


    // --------------------------- 【语法糖方法】 -----------------------------

    /**
     * 快速转为异常
     *
     * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
     * @return 异常
     */
    default BaseRuntimeException toException(Object... args) {
        return new BaseRuntimeException(this, args);
    }

    /**
     * 快速转为异常
     *
     * @param t    上级异常
     * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
     * @return 异常
     */
    default BaseRuntimeException toException(Throwable t, Object... args) {
        return new BaseRuntimeException(this, t, args);
    }


    /**
     * --------------------------- 【标识处理成功的返回值】 -----------------------------
     */
    class SuccessCode implements ErrorCode {

        @Nonnull
        @Override
        public String getCode() {
            return SUCCESS_CODE;
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public Level getLogLevel() {
            return Level.DEBUG;
        }

        @Override
        public HttpStatus getHttpStatusCode() {
            return HttpStatus.OK;
        }


        /**
         * 快速转为异常
         *
         * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
         * @return 异常
         */
        @Override
        public BaseRuntimeException toException(Object... args) {
            throw new IllegalCallerException("SuccessCode can't convert to an exception!");
        }

        /**
         * 快速转为异常
         *
         * @param t    上级异常
         * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
         * @return 异常
         */
        @Override
        public BaseRuntimeException toException(Throwable t, Object... args) {
            throw new IllegalCallerException("Success can't convert to an exception!");
        }

    }

    class IdempotentSuccessCode extends SuccessCode {
        @Override
        public String getMessage() {
            return "idempotent success";
        }
    }

}
