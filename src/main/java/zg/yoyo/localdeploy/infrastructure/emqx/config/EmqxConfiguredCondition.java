package zg.yoyo.localdeploy.infrastructure.emqx.config;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EmqxConfiguredCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            EmqxProperties props = Binder.get(context.getEnvironment())
                    .bind("spring.emqx", EmqxProperties.class)
                    .orElse(null);
            return props != null
                    && notEmpty(props.getBrokerUrl())
                    && notEmpty(props.getClientId())
                    && notEmpty(props.getUsername())
                    && notEmpty(props.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
