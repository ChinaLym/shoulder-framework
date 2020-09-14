package org.shoulder.autoconfigure.condition;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.context.AppInfo;
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
@Slf4j
public class OnClusterCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext conditionContext, @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        MergedAnnotation<ConditionalOnCluster> mergedAnnotation =
            annotatedTypeMetadata.getAnnotations().get(ConditionalOnCluster.class);
        if (!mergedAnnotation.isPresent()) {
            return true;
        }
        ConditionalOnCluster condition = mergedAnnotation.synthesize();
        boolean clusterMode = AppInfo.cluster();
        return clusterMode == condition.cluster();

    }
}
