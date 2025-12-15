package zg.yoyo.localdeploy.infrastructure.minio.config;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MinioConfiguredCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            MinioProperties props = Binder.get(context.getEnvironment())
                    .bind("minio", MinioProperties.class)
                    .orElse(null);
            return props != null && props.isConfigured();
        } catch (Exception e) {
            return false;
        }
    }
}
