package org.shoulder.autoconfigure.condition;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 判断认证方式
 *
 * @author lym
 */
public class OnAuthTypeCondition implements Condition {

    @Override
    public boolean matches(@Nonnull ConditionContext conditionContext, @Nonnull AnnotatedTypeMetadata annotatedTypeMetadata) {
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
