package org.shoulder.autoconfigure.condition;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * 判断是否开启了集群模式
 *
 * @author lym
 */
public class OnAuthTypeCondition implements Condition {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean matches(@NonNull ConditionContext conditionContext, @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        MergedAnnotation<ConditionalOnAuthType> mergedAnnotation =
            annotatedTypeMetadata.getAnnotations().get(ConditionalOnAuthType.class);
        if (!mergedAnnotation.isPresent()) {
            return true;
        }
        ConditionalOnAuthType condition = mergedAnnotation.synthesize();
        String configType = conditionContext.getEnvironment().getProperty("shoulder.security.auth.type");
        return condition.type().name().equalsIgnoreCase(configType);

    }
}
