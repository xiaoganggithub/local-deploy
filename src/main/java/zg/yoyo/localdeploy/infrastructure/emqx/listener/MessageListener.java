package zg.yoyo.localdeploy.infrastructure.emqx.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import zg.yoyo.localdeploy.infrastructure.emqx.annotation.EmqxListener;
import zg.yoyo.localdeploy.interfaces.message.dto.MessageDto;

/**
 * @author zhenggang
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageListener {

    private final ObjectMapper objectMapper;

    @EmqxListener(topic = "emat-test-topic", qos = 1)
    public void onMessage(String topic, MqttMessage message) {
        try {
            byte[] payload = message.getPayload();
            MessageDto dto = objectMapper.readValue(payload, MessageDto.class);
            log.info("Received message on topic={}, payload={}", topic, objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
        }
    }
}
