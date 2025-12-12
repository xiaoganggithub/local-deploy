package zg.yoyo.localdeploy.infrastructure.emqx;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmqxPublisher {

    private final String broker;
    private final String clientId;
    private final MqttConnectOptions mqttConnectOptions;
    private final String emqxTopic;
    private final int emqxQos;
    
    private MqttClient mqttClient;
    private boolean isConnected = false;

    /**
     * Connect to EMQX broker
     */
    public void connect() {
        try {
            if (mqttClient == null) {
                mqttClient = new MqttClient(broker, clientId + "-publisher", new MemoryPersistence());
                mqttClient.setCallback(new EmqxCallbackHandler());
            }
            
            if (!mqttClient.isConnected()) {
                mqttClient.connect(mqttConnectOptions);
                isConnected = true;
                log.info("EMQX publisher connected to broker: {}", broker);
            }
        } catch (MqttException e) {
            log.error("Failed to connect EMQX publisher: {}", e.getMessage(), e);
            isConnected = false;
        }
    }

    /**
     * Disconnect from EMQX broker
     */
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                isConnected = false;
                log.info("EMQX publisher disconnected from broker: {}", broker);
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect EMQX publisher: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish message to default topic
     * 
     * @param message message content
     */
    public void publish(String message) {
        publish(emqxTopic, message, emqxQos);
    }

    /**
     * Publish message to specified topic with QoS
     * 
     * @param topic topic name
     * @param message message content
     * @param qos quality of service level
     */
    public void publish(String topic, String message, int qos) {
        try {
            if (!isConnected) {
                log.warn("EMQX publisher not connected, trying to reconnect...");
                connect();
            }

            if (isConnected && mqttClient != null) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(qos);
                mqttClient.publish(topic, mqttMessage);
                log.info("Message published successfully. Topic: {}, QoS: {}, Message: {}", topic, qos, message);
            } else {
                log.error("Failed to publish message - publisher not connected");
            }
        } catch (MqttException e) {
            log.error("Failed to publish message: {}", e.getMessage(), e);
            isConnected = false;
        }
    }

    /**
     * Check if publisher is connected
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected && mqttClient != null && mqttClient.isConnected();
    }
}