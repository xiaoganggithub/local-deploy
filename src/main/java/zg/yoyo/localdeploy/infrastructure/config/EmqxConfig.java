package zg.yoyo.localdeploy.infrastructure.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxProperties;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxConfiguredCondition;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxClientFactory;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxTemplate;
import zg.yoyo.localdeploy.infrastructure.emqx.core.PahoEmqxClientFactory;
import zg.yoyo.localdeploy.infrastructure.emqx.health.EmqxHealthIndicator;

@Configuration
@EnableConfigurationProperties(EmqxProperties.class)
@Slf4j
@Conditional(EmqxConfiguredCondition.class)
public class EmqxConfig {

    @Bean
    public MqttConnectOptions mqttConnectOptions(EmqxProperties props) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(props.getUsername());
        options.setPassword(props.getPassword().toCharArray());
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        options.setAutomaticReconnect(true);
        if (props.isSsl() || props.getBrokerUrl().startsWith("ssl")) {
            options.setSocketFactory(javax.net.ssl.SSLSocketFactory.getDefault());
        }
        log.info("EMQX MqttConnectOptions initialized for broker {}", props.getBrokerUrl());
        return options;
    }

    @Bean
    public EmqxClientFactory emqxClientFactory(EmqxProperties props, MqttConnectOptions options) {
        log.info("EMQX client factory initialized");
        return new PahoEmqxClientFactory(props, options);
    }

    @Bean
    public EmqxTemplate emqxTemplate(EmqxClientFactory factory, EmqxProperties props, MeterRegistry meterRegistry) {
        log.info("EMQX template initialized");
        return new EmqxTemplate(factory, props, meterRegistry);
    }

    @Bean
    public EmqxHealthIndicator emqxHealthIndicator(EmqxTemplate template) {
        return new EmqxHealthIndicator(template);
    }
}
