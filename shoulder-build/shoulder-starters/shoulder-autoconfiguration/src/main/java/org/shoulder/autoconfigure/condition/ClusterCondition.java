package org.shoulder.autoconfigure.condition;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.context.ApplicationInfo;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * 集群条件
 * @author lym
 */
@Slf4j
public class ClusterCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext conditionContext, @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        MergedAnnotation<ConditionalOnCluster> mergedAnnotation =
            annotatedTypeMetadata.getAnnotations().get(ConditionalOnCluster.class);
        if(!mergedAnnotation.isPresent()){
            return true;
        }
        ConditionalOnCluster condition = mergedAnnotation.synthesize();
        boolean clusterMode = ApplicationInfo.cluster();
        return clusterMode == condition.cluster();

    }
}
