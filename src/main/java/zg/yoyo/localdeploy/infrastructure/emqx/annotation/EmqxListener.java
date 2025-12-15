package zg.yoyo.localdeploy.infrastructure.emqx.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhenggang
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EmqxListener {
    String topic();
    int qos() default 1;
}
