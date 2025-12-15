package zg.yoyo.localdeploy.infrastructure.emqx.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MessageListenerTest {

    @Test
    void onMessageParsesJson() {
        ObjectMapper mapper = new ObjectMapper();
        MessageListener listener = new MessageListener(mapper);
        String json = "{\"id\":\"1\",\"type\":\"t\",\"data\":{\"k\":\"v\"}}";
        MqttMessage msg = new MqttMessage(json.getBytes());
        assertDoesNotThrow(() -> listener.onMessage("app/messages", msg));
    }
}
