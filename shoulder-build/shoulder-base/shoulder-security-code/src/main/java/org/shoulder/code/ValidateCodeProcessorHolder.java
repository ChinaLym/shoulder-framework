package org.shoulder.code;

import org.shoulder.code.exception.NoSuchValidateCodeProcessorException;
import org.shoulder.code.exception.ValidateCodeAuthenticationException;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供处理器
 *
 * @author lym
 */
public class ValidateCodeProcessorHolder implements InitializingBean {

    /**
     * 所有的验证码处理器
     */
    private List<ValidateCodeProcessor> allProcessors;

    private Map<String, ValidateCodeProcessor> processorMap;

    public ValidateCodeProcessorHolder(List<ValidateCodeProcessor> allProcessors) {
        this.allProcessors = allProcessors;
    }

    /**
     * 获取验证码处理器
     *
     * @throws ValidateCodeAuthenticationException 没有对应的验证码处理器
     */
    @NonNull
    public ValidateCodeProcessor getProcessor(String type) throws NoSuchValidateCodeProcessorException {
        ValidateCodeProcessor processor = processorMap.get(type);
        if (processor == null) {
            // "ValidateCodeProcessor(type) not exist." 避免 NPE
            throw new NoSuchValidateCodeProcessorException("not support such validateCode(" + type + ")");
        }
        return processor;
    }


    @Override
    public void afterPropertiesSet() {
        processorMap = new HashMap<>(allProcessors.size());
        for (ValidateCodeProcessor processor : allProcessors) {
            processorMap.put(processor.getType(), processor);
        }
    }

    public List<ValidateCodeProcessor> getAllProcessors() {
        return allProcessors;
    }
}
