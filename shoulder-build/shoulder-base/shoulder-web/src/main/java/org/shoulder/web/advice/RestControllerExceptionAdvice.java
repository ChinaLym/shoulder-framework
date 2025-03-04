package org.shoulder.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * RestController 全局异常处理器 - 请求方错误，提供默认统一场景错误返回值
 * 不同 RestControllerAdvice 类中的异常处理器优先级：与 @Order 接口定义有关，默认最低，用户可以定义，以覆盖框架实现
 * <p>
 * 组装响应时，未根据返回值类型判断，统一返回 JSON 格式标准响应
 *
 * @author lym
 */
@SuppressWarnings("rawtypes")
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class RestControllerExceptionAdvice {

    private final Logger log = ShoulderLoggers.SHOULDER_WEB;

    /**
     * 缺少参数
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public BaseResult paramsMissingHandler(MissingServletRequestParameterException e) {
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_BLANK, e, e.getParameterName());
        log.info(stdEx);
        return stdEx.toResponse();
    }

    /**
     * 请求体解析参数时失败或缺少参数。如 POST
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResult messageNotReadableHandler(HttpMessageNotReadableException e) {
        final String springErrorTipHeader = "Could not read document:";
        final String errorStackSplit = " at ";
        String message = e.getMessage();
        BaseRuntimeException stdEx = new BaseRuntimeException(CommonErrorCodeEnum.PARAM_BODY_NOT_READABLE, e, e.getMessage());
        if (StringUtils.contains(message, springErrorTipHeader)) {
            String errorInfo = StringUtils.subBetween(message, springErrorTipHeader, errorStackSplit);
            stdEx.setArgs(errorInfo);
        }
        log.info(stdEx);
        return stdEx.toResponse();
    }

    /**
     * JSON包装类 DTO 校验异常
     *
     * @return 基础返回类型，增加了解析后的错误信息
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public BaseResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String firstErrorInfo = getFirstErrorDescription(e.getBindingResult());
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_ILLEGAL, e, firstErrorInfo);
        log.info(stdEx);
        return stdEx.toResponse();
    }


    /**
     * 缺少参数
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public BaseResult illegalArgumentHandler(IllegalArgumentException e) {
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_ILLEGAL, e, e.getMessage());
        log.info(stdEx);
        return stdEx.toResponse();
    }


    /**
     * 字段类型不匹配
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public BaseResult bindExceptionHandler(BindException e) {
        String firstErrorInfo = getFirstErrorDescription(e.getBindingResult());
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_ILLEGAL, e, firstErrorInfo);
        log.info(stdEx);
        return stdEx.toResponse();
    }


    /**
     * jsr303 验证不通过
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public BaseResult constraintViolationExceptionHandler(ConstraintViolationException e) {
        // 默认使用 hibernate，未做其他实现判断（可能未开启快速失败，因此会有多个错误原因，值取第一个）
        ConstraintViolationImpl firstConstraintViolation = (ConstraintViolationImpl) e.getConstraintViolations()
                .stream().findFirst().orElse(null);
        assert firstConstraintViolation != null;
        // 使用校验处类的日志记录器打印日志 getPropertyPath().toString() 也行
        NodeImpl node = ((PathImpl) firstConstraintViolation.getPropertyPath()).getLeafNode();
        String paramName = node.getName();
        // 可以在这里打印方法名，必要不大，暂未实现
        String msgInAnnotation = firstConstraintViolation.getMessage();
        String msg = StringUtils.isEmpty(msgInAnnotation) ? ParamErrorCodeEnum.PARAM_ILLEGAL.getMessage() : msgInAnnotation;
        Logger logger = LoggerFactory.getLogger(firstConstraintViolation.getRootBeanClass().getName());
        if (logger.isInfoEnabled()) {
            // 这里堆栈信息不必打印
            logger.infoWithErrorCode(ParamErrorCodeEnum.PARAM_ILLEGAL.getCode(),
                    "RestControllerExceptionAdvice - " + paramName + " - " + msg);
        }
        return new BaseResult<>(ParamErrorCodeEnum.PARAM_ILLEGAL.getCode(), msg, new Object[]{paramName});
    }

    /**
     * jsr303 验证时发生异常，需要关注
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ValidationException.class})
    public BaseResult ValidationExceptionHandler(ValidationException e) {
        String stageErrorMsg = e.getMessage();
        String rootCauseErrorMsg = String.valueOf(Optional.ofNullable(NestedExceptionUtils.getRootCause(e)).map(Throwable::getMessage));
        AppLoggers.APP_WARN.warnWithErrorCode(CommonErrorCodeEnum.ILLEGAL_PARAM.getCode(),
                "ValidationException for {}, rootMsg: {}, please check the stackTrace.", stageErrorMsg, rootCauseErrorMsg, e);
        return new BaseResult<>(CommonErrorCodeEnum.ILLEGAL_PARAM);
    }

    /**
     * 获取绑定结果错误中第一个引发错误的描述信息
     *
     * @param bindingResult 绑定结果
     * @return 第一个引发错误的描述信息
     */
    private String getFirstErrorDescription(BindingResult bindingResult) {
        ObjectError error = bindingResult.getAllErrors().get(0);
        String msg = error.getDefaultMessage();
        String objectName = StringUtils.trim(error.getObjectName());
        if (StringUtils.isNotBlank(objectName)) {
            objectName += ".";
        }
        if (error instanceof FieldError) {
            // fieldError
            String field = StringUtils.trim(((FieldError) error).getField());
            if (StringUtils.isNotBlank(field)) {
                field += ".";
            }
            return objectName + field + msg;
        } else {
            // 普通Error
            String code = error.getCode();
            return objectName + code + msg;
        }
    }


    /**
     * 请求方法不允许
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public BaseResult methodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
        String support = "";
        if (CollectionUtils.isNotEmpty(e.getSupportedHttpMethods())) {
            StringJoiner sj = new StringJoiner(",", "'", "'");
            e.getSupportedHttpMethods().stream().map(HttpMethod::name).forEach(sj::add);
            support = sj.toString();
        }
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.REQUEST_METHOD_MISMATCH, e, e.getMethod(), support);
        log.warn(ex);
        return ex.toResponse();
    }


    /**
     * 参数类型不匹配，如希望 int 传来 String
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResult methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        BaseRuntimeException ex =
                new BaseRuntimeException(ParamErrorCodeEnum.PARAM_TYPE_NOT_MATCH, e,
                        e.getName(), e.getValue(), e.getRequiredType() == null ? null : e.getRequiredType().getName());
        log.info(ex);
        return ex.toResponse();
    }


    /**
     * Content-Type 不正确
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public BaseResult httpMediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.CONTENT_TYPE_INVALID, e, String.valueOf(e.getContentType()));
        log.info(ex);
        return ex.toResponse();
    }

    /**
     * @ExceptionHandler(MissingServletRequestPartException.class)
     * 多线程操作已经销毁的request，编码问题
     */


    /**
     * 上传文件解析错误，请求未携带文件、上传文件过大等
     * 注意：【未知bug】spring mvc（tomcat）中上传文件超出限制过多可能抛出3次异常，可能是由于分片并发传输导致的
     */
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MultipartException.class)
    public BaseResult multipartException(MultipartException e) {
        // MultipartException 只有一个子类 MaxUploadSizeExceededException，很可能是上传文件过大，或不能从请求中解析出来
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.MULTIPART_INVALID, e);
        log.warn(ex);
        return ex.toResponse();
    }

    /**
     * 其他异常
     * 对于这类不明确的异常，原始报错消息改成未知异常，日志打印详细内容并使用未知错误码，以避免暴露堆栈信息等
     * 【可选】ClientAbortException tomcat中客户端连接断开，如浏览器请求了，还没响应就关闭了，服务器返回时发现response不能写
     */
    @ExceptionHandler(Exception.class)
    public BaseResult otherExceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 暂不考虑不是 json 响应
        BaseRuntimeException ex;
        if (e instanceof ErrorCode errorCode) {
            // 符合规范定义的错误码，按照错误码日志级别记录
            log.log(errorCode);
            ex = new BaseRuntimeException(e);
        } else {
            // 未知异常
            ex = new BaseRuntimeException(CommonErrorCodeEnum.UNKNOWN, e);
            log.error(CommonErrorCodeEnum.UNKNOWN.getCode(), e.getMessage(), e);
        }
        return ex.toResponse();
    }

    /**
     * 其他：数据库常见异常，主要由开发者导致
     *
     * 多发于 mapper 和接口不匹配（参数）
     * PersistenceException
     * 开发者 mybatis 用法错误
     * MyBatisSystemException
     * 数据库连不上、开发者用法导致 sql问题
     * SQLException
     * 开发者配置信息不全，完整性（必填为空），唯一性约束（主键不完整）
     * DataIntegrityViolationException
     */

    /**
     * 数据库保存失败 (spring data 中不会抛 SQLException )
     */
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public BaseResult sqlExceptionHandler(SQLException e) {
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.DATA_STORAGE_FAIL, e, e.getMessage());
        log.error(ex);
        return ex.toResponse();
    }

}
