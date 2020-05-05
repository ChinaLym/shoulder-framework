package org.shoulder.web;

/**
 * 全局异常处理
 * {@link @Controller}、{@link @RestController}、{@link @RequestMapping} 方法的参数验证相关异常
 * <p>还需要需要声明为Bean</p>
 *
 * @author lym
 */
/*
@RestControllerAdvice
public class GlobalExceptionAdvice {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    */
/**
     * GET方式缺少参数
     *
     * @return .
     *//*

    */
/*@ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public BaseResponse<String> paramsMissingHandler(MissingServletRequestParameterException e) {
        String msg = ExceptionUtil.generateExceptionMessage( CommonErrorEnum.PARAMETER_MISSING.getMessage(), )
        String msg =.(e.getParameterName());
        return new BaseResponse<>(CommonErrorCode.PARAMS_BLANK.getCode(), msg,
                e.getMessage());
    }*//*


    */
/**
     * .POST方式缺少参数或解析json失败
     *
     * @param e .
     *
     * @return .
     *//*

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<String> messageNotReadableHandler(HttpMessageNotReadableException e) {
        LOGGER.errorWithErrorCode(ErrorCodeBaseConstant
                        .getInfosightErrorCode(CommonErrorCode.PARAMS_HTTP_MESSAGE_NOT_READABLE.getCode()), "{}",
                e.getMessage());
        return new BaseResponse<>(CommonErrorCode.PARAMS_HTTP_MESSAGE_NOT_READABLE.getCode(),
                CommonErrorCode.PARAMS_HTTP_MESSAGE_NOT_READABLE.getMsg());
    }

    */
/**
     * .捕获和处理参数验证中的方法参数无效异常MethodArgumentNotValidException case: JSON格式的包装类异常，如@ResponsBody @Valid User user
     *
     * @param e 异常对象
     *
     * @return 基础返回类型，增加了解析后的错误信息
     *//*

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public BaseResponse<Map<String, String>> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException e) {
        Map<String, String> errorMap = getErrorMsg(e.getBindingResult());
        return new BaseResponse<>(CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getCode(),
                CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getMsg(), errorMap);
    }

    */
/**
     * .参数格式转换异常 捕获和处理参数验证中的异常BindException case：表单格式的包装类异常，如@Valid User user
     *
     * @param e 异常对象
     *
     * @return 基础返回类型，增加了解析后的错误信息
     *//*

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})
    public BaseResponse<Map<String, String>> bindExceptionHandler(BindException e) {
        Map<String, String> errorMap = getErrorMsg(e.getBindingResult());
        return new BaseResponse<>(CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getCode(),
                CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getMsg(), errorMap);
    }


    */
/**
     * .参数转换异常 捕获和处理参数验证中的违反实体定义的约束异常ConstraintViolationException case：@RequestParam的原生注解异常
     *
     * @param e 异常对象
     *
     * @return 基础返回类型，增加了解析后的错误信息
     *//*

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public BaseResponse<Map<String, String>> constraintViolationExceptionHandler(ConstraintViolationException e) {
        Map<String, String> errorMap = new HashMap<>(16);
        for (ConstraintViolation<?> error : e.getConstraintViolations()) {
            String code = error.getPropertyPath().toString();
            code = StringUtils.substringAfter(code, ".");
            String msg = error.getMessageTemplate();
            errorMap.put(code, msg);
        }
        return new BaseResponse<>(CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getCode(),
                CommonErrorCode.SYSTEM_PARAMETER_ILLEGAL_ERROR.getMsg(), errorMap);
    }


    */
/**
     * .提取异常中的信息，封装成map格式返回
     *
     * @param bindingResult BindingResult
     *
     * @return map格式
     *//*

    private Map<String, String> getErrorMsg(BindingResult bindingResult) {

        Map<String, String> errorMap = new HashMap<>(16);

        for (ObjectError error : bindingResult.getAllErrors()) {
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
                errorMap.put(objectName + field, msg);
            } else {
                // 普通Error
                String code = error.getCode();
                errorMap.put(objectName + code, msg);
            }
        }
        return errorMap;
    }

    */
/**
     * .请求方式不匹配
     *
     * @param e 异常对象
     *
     * @return 基础返回类型，增加了解析后的错误信息
     *//*

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public BaseResponse<String> methodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
        return new BaseResponse<>(CommonErrorCode.NETWORK_NOT_ALLOW_METHOD.getCode(),
                CommonErrorCode.NETWORK_NOT_ALLOW_METHOD.getMsg(), "");
    }
}
*/
