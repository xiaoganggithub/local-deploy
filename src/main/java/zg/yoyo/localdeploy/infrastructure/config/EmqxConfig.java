package zg.yoyo.localdeploy.infrastructure.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class EmqxConfig {

    @Value("${emqx.broker}")
    private String broker;
    
    @Value("${emqx.client-id}")
    private String clientId;
    
    @Value("${emqx.username}")
    private String username;
    
    @Value("${emqx.password}")
    private String password;
    
    @Value("${emqx.topic}")
    private String topic;
    
    @Value("${emqx.qos}")
    private int qos;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        options.setAutomaticReconnect(true);
        return options;
    }
    
    @Bean
    public String broker() {
        return broker;
    }
    
    @Bean
    public String clientId() {
        return clientId;
    }
    
    @Bean
    public String emqxTopic() {
        return topic;
    }
    
    @Bean
    public int emqxQos() {
        return qos;
    }
}