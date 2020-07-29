package org.shoulder.autoconfigure.condition;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.context.ApplicationInfo;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;

/**
 * 集群条件
 * @author lym
 */
@Slf4j
public class ClusterCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext conditionContext, @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean clusterMode = ApplicationInfo.cluster();
        clusterMode = Boolean.parseBoolean(
            conditionContext.getEnvironment().getProperty("shoulder.application.cluster"));
        return clusterMode;

    }
}
