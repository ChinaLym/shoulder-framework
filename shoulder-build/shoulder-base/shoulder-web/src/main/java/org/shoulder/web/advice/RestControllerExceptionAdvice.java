package org.shoulder.web.advice;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;

/**
 * RestController 全局异常处理器 - 请求方错误，提供默认统一场景错误返回值
 *
 * @author lym
 */
@RestControllerAdvice
public class RestControllerExceptionAdvice {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String GLOBAL_EXCEPTION_HANDLER_TIP = "RestControllerExceptionAdvice - ";

    @Autowired
    private Translator translator;


    /**
     * 缺少参数
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public RestResult<Object[]> paramsMissingHandler(MissingServletRequestParameterException e) {
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_BLANK, e, e.getParameterName());
        log.info(stdEx);
        return stdEx.toResponse();
    }

    /**
     * 请求体解析参数时失败或缺少参数。如 POST
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public RestResult<Object[]> messageNotReadableHandler(HttpMessageNotReadableException e) {
        final String springErrorTipHeader = "Could not read document:";
        final String errorStackSplit = " at ";
        String message = e.getMessage();
        BaseRuntimeException stdEx = new BaseRuntimeException(CommonErrorCodeEnum.PARAM_BODY_NOT_READABLE, e);
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
    public RestResult<Object[]> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String firstErrorInfo = getFirstErrorDescription(e.getBindingResult());
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_INVALID, e, firstErrorInfo);
        log.info(stdEx);
        return stdEx.toResponse();
    }

    /**
     * 字段类型不匹配
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public RestResult<Object[]> bindExceptionHandler(BindException e) {
        String firstErrorInfo = getFirstErrorDescription(e.getBindingResult());
        BaseRuntimeException stdEx = new BaseRuntimeException(ParamErrorCodeEnum.PARAM_INVALID, e, firstErrorInfo);
        log.info(stdEx);
        return stdEx.toResponse();
    }


    /**
     * jsr303 验证不通过
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public RestResult<Object[]> constraintViolationExceptionHandler(ConstraintViolationException e) {
        // 这里取了第一个错误作为校验错误原因，且默认使用 hibernate，未做其他实现判断
        ConstraintViolationImpl firstConstraintViolation = (ConstraintViolationImpl) e.getConstraintViolations()
            .stream().findFirst().orElse(null);
        assert firstConstraintViolation != null;
        // 使用校验处类的日志记录器打印日志
        NodeImpl node = ((PathImpl) firstConstraintViolation.getPropertyPath()).getLeafNode();
        String paramName = node.getName();
        // 可以在这里打印方法名，必要不大，暂未实现
        String msgInAnnotation = firstConstraintViolation.getMessage();
        String msg = StringUtils.isEmpty(msgInAnnotation) ? ParamErrorCodeEnum.PARAM_INVALID.getMessage() : msgInAnnotation;
        Logger logger = LoggerFactory.getLogger(firstConstraintViolation.getRootBeanClass().getName());
        if (logger.isInfoEnabled()) {
            String logMessage = translator.getMessage(msg, paramName, AppInfo.defaultLocale());
            // 这里堆栈信息不必打印
            logger.infoWithErrorCode(ParamErrorCodeEnum.PARAM_INVALID.getCode(), GLOBAL_EXCEPTION_HANDLER_TIP + logMessage);
        }
        return new RestResult<>(ParamErrorCodeEnum.PARAM_INVALID.getCode(), msg, new Object[]{paramName});
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
    public RestResult methodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.REQUEST_METHOD_MISMATCH, e);
        log.warn(ex);
        return ex.toResponse();
    }


    /**
     * 参数类型不匹配，如希望 int 传来 String
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RestResult methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
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
    public RestResult httpMediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
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
    public RestResult multipartException(MultipartException e) {
        // MultipartException 只有一个子类 MaxUploadSizeExceededException，很可能是上传文件过大，或不能从请求中解析出来
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.MULTIPART_INVALID, e);
        log.warn(ex);
        return ex.toResponse();
    }

    /**
     * 其他异常
     * todo ClientAbortException tomcat —— 客户端连接断开，如浏览器请求了，还没响应就关闭了，服务器返回时发现response不能写
     */
    @ExceptionHandler(Exception.class)
    public RestResult otherExceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!StringUtils.containsIgnoreCase(response.getContentType(), "json")) {
            // 不是 json 响应
            log.error(CommonErrorCodeEnum.UNKNOWN.toException(e));
            throw e;
        }
        BaseRuntimeException ex;
        if (e instanceof ErrorCode) {
            // 符合规范定义的错误码，按照错误码日志级别记录
            ErrorCode errorCode = (ErrorCode) e;
            log.log(errorCode);
            ex = new BaseRuntimeException(e);
            response.setStatus(errorCode.getHttpStatusCode().value());
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
     * todo DataAccessException spring 的数据持久层异常基类（依赖数据库）
     */
    /**
     * 数据库保存失败
     */
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public RestResult sqlExceptionHandler(SQLException e) {
        BaseRuntimeException ex = new BaseRuntimeException(CommonErrorCodeEnum.PERSISTENCE_TO_DB_FAIL, e);
        log.error(ex);
        return ex.toResponse();
    }

}
