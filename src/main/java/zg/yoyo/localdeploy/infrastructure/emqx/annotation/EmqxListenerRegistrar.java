package zg.yoyo.localdeploy.infrastructure.emqx.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(EmqxTemplate.class)
public class EmqxListenerRegistrar implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final EmqxTemplate template;
    private final Environment environment;

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.stream(beanNames).forEach(name -> {
            Object bean = applicationContext.getBean(name);
            for (Method m : bean.getClass().getMethods()) {
                EmqxListener ann = m.getAnnotation(EmqxListener.class);
                if (ann != null) {
                    try {
                        String resolvedTopic = environment != null ? environment.resolvePlaceholders(ann.topic()) : ann.topic();
                        template.subscribe(resolvedTopic, ann.qos(), (topic, message) -> invoke(bean, m, topic, message));
                        log.info("Registered EmqxListener: bean={} method={} topic={} qos={}", name, m.getName(), resolvedTopic, ann.qos());
                    } catch (Exception e) {
                        log.error("Failed to register EmqxListener: {}", e.getMessage(), e);
                    }
                }
            }
        });
    }

    private void invoke(Object bean, Method method, String topic, MqttMessage message) {
        try {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 2 && params[0] == String.class && params[1] == MqttMessage.class) {
                method.invoke(bean, topic, message);
            } else if (params.length == 1 && params[0] == String.class) {
                method.invoke(bean, topic);
            } else if (params.length == 1 && params[0] == MqttMessage.class) {
                method.invoke(bean, message);
            } else {
                method.invoke(bean);
            }
        } catch (Exception e) {
            log.error("Error invoking EmqxListener method: {}", e.getMessage(), e);
        }
    }
}
