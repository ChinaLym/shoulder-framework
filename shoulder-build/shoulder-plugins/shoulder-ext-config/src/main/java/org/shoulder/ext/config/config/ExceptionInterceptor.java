package org.shoulder.ext.config.config;

import com.google.common.collect.Lists;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * @author lym
 */
public class ExceptionInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ExceptionInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (IllegalArgumentException e) {
            log.error(e);
            return BaseResult.error(ParamErrorCodeEnum.PARAM_ILLEGAL);
        } catch (ConstraintViolationException e) {
            log.error(e);
            return handleConstraintViolationException(e);
        } catch (BaseRuntimeException e) {
            log.error(e);
            return BaseResult.error(e);
        } catch (Exception e) {
            log.error(e);
            return BaseResult.error(CommonErrorCodeEnum.UNKNOWN);
        }
    }

    /**
     * Handle constraint violation exception backstage result.
     *
     * @param e the e
     * @return the backstage result
     */
    public BaseResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.error(e);
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> messages = Lists.newArrayList();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            messages.add(constraintViolation.getPropertyPath().toString() + " " + constraintViolation.getMessage());
        }
        String message = StringUtils.join(messages, ",");
        return BaseResult.error(ParamErrorCodeEnum.PARAM_ILLEGAL, message);
    }

}