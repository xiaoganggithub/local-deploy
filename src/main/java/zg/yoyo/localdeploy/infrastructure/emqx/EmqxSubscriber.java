package zg.yoyo.localdeploy.infrastructure.emqx;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmqxSubscriber {

    private final String broker;
    private final String clientId;
    private final MqttConnectOptions mqttConnectOptions;
    private final String emqxTopic;
    private final int emqxQos;
    
    private MqttClient mqttClient;
    private boolean isConnected = false;
    private final Set<String> subscribedTopics = new HashSet<>();

    /**
     * Connect to EMQX broker
     */
    public void connect() {
        try {
            if (mqttClient == null) {
                mqttClient = new MqttClient(broker, clientId + "-subscriber", new MemoryPersistence());
                mqttClient.setCallback(new EmqxSubscriberCallbackHandler());
            }
            
            if (!mqttClient.isConnected()) {
                mqttClient.connect(mqttConnectOptions);
                isConnected = true;
                log.info("EMQX subscriber connected to broker: {}", broker);
            }
        } catch (MqttException e) {
            log.error("Failed to connect EMQX subscriber: {}", e.getMessage(), e);
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
                subscribedTopics.clear();
                log.info("EMQX subscriber disconnected from broker: {}", broker);
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect EMQX subscriber: {}", e.getMessage(), e);
        }
    }

    /**
     * Subscribe to the default topic
     */
    public void subscribe() {
        subscribe(emqxTopic, emqxQos);
    }

    /**
     * Subscribe to a specific topic with QoS
     * 
     * @param topic topic name
     * @param qos quality of service level
     */
    public void subscribe(String topic, int qos) {
        try {
            if (!isConnected) {
                log.warn("EMQX subscriber not connected, trying to reconnect...");
                connect();
            }

            if (isConnected && mqttClient != null) {
                mqttClient.subscribe(topic, qos);
                subscribedTopics.add(topic);
                log.info("Subscribed to topic: {} with QoS: {}", topic, qos);
            } else {
                log.error("Failed to subscribe - subscriber not connected");
            }
        } catch (MqttException e) {
            log.error("Failed to subscribe to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Subscribe to multiple topics
     * 
     * @param topics array of topic names
     * @param qos array of QoS levels for each topic
     */
    public void subscribe(String[] topics, int[] qos) {
        try {
            if (!isConnected) {
                log.warn("EMQX subscriber not connected, trying to reconnect...");
                connect();
            }

            if (isConnected && mqttClient != null) {
                mqttClient.subscribe(topics, qos);
                for (String topic : topics) {
                    subscribedTopics.add(topic);
                }
                log.info("Subscribed to multiple topics: {}", String.join(", ", topics));
            } else {
                log.error("Failed to subscribe - subscriber not connected");
            }
        } catch (MqttException e) {
            log.error("Failed to subscribe to multiple topics: {}", e.getMessage(), e);
        }
    }

    /**
     * Unsubscribe from the default topic
     */
    public void unsubscribe() {
        unsubscribe(emqxTopic);
    }

    /**
     * Unsubscribe from a specific topic
     * 
     * @param topic topic name
     */
    public void unsubscribe(String topic) {
        try {
            if (isConnected && mqttClient != null && subscribedTopics.contains(topic)) {
                mqttClient.unsubscribe(topic);
                subscribedTopics.remove(topic);
                log.info("Unsubscribed from topic: {}", topic);
            } else if (!subscribedTopics.contains(topic)) {
                log.warn("Not subscribed to topic: {}", topic);
            } else {
                log.error("Failed to unsubscribe - subscriber not connected");
            }
        } catch (MqttException e) {
            log.error("Failed to unsubscribe from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Check if subscriber is connected
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected && mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Get all subscribed topics
     * 
     * @return set of subscribed topic names
     */
    public Set<String> getSubscribedTopics() {
        return new HashSet<>(subscribedTopics);
    }

    /**
     * Subscriber specific callback handler for incoming messages
     */
    private static class EmqxSubscriberCallbackHandler extends EmqxCallbackHandler {
        @Override
        public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
            String messageContent = new String(message.getPayload());
            log.info("📩 Received message: Topic={}, QoS={}, Message={}", 
                    topic, message.getQos(), messageContent);
            
            // Additional message processing can be added here
            processMessage(topic, messageContent);
        }

        /**
         * Process incoming messages based on topic
         * 
         * @param topic topic name
         * @param message message content
         */
        private void processMessage(String topic, String message) {
            // Example: Different processing logic based on topic
            if (topic.contains("demo")) {
                log.info("🔧 Processing demo message: {}", message);
                // Demo-specific processing
            } else {
                log.info("🔧 Processing general message: {}", message);
                // General processing
            }
        }
    }
}