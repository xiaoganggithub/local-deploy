package zg.yoyo.localdeploy.infrastructure.emqx;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmqxCallbackHandler implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("EMQX connection lost: {}", cause.getMessage(), cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // This method is mainly used by subscribers, but included for completeness
        log.info("Message arrived. Topic: {}, QoS: {}, Message: {}", 
                topic, message.getQos(), new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Delivery complete. Message ID: {}", token.getMessageId());
        String[] topics = token.getTopics();
        if (topics != null) {
            for (String topic : topics) {
                log.debug("Delivered message to topic: {}", topic);
            }
        }
    }
}