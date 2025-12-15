package zg.yoyo.localdeploy.infrastructure.emqx.core;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxProperties;

/**
 * @author zhenggang
 */
@Slf4j
public class EmqxTemplate {

    private final EmqxClientFactory factory;
    @Getter
    private EmqxProperties properties;
    private final Counter publishCounter;
    private final Counter receiveCounter;

    public EmqxTemplate(EmqxClientFactory factory, EmqxProperties properties, MeterRegistry meterRegistry) {
        this.factory = factory;
        this.properties = properties;
        this.publishCounter = meterRegistry != null ? Counter.builder("emqx_publish_total").register(meterRegistry) : null;
        this.receiveCounter = meterRegistry != null ? Counter.builder("emqx_receive_total").register(meterRegistry) : null;
    }

    public boolean isConnected() {
        try {
            return factory.getClient().isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public void reconfigure(EmqxProperties props) {
        this.properties = props;
        factory.close();
        log.info("EMQX properties reconfigured");
    }

    public void subscribe(String topic, int qos, BiConsumer<String, MqttMessage> handler) throws Exception {
        MqttAsyncClient client = factory.getClient();
        IMqttMessageListener listener = (t, msg) -> {
            if (receiveCounter != null) {
                receiveCounter.increment();
            }
            try {
                handler.accept(t, msg);
            } catch (Exception e) {
                log.error("EMQX listener error: {}", e.getMessage(), e);
            }
        };
        IMqttToken token = client.subscribe(topic, qos, listener);
        token.waitForCompletion();
        log.info("Subscribed to topic: {} qos={}", topic, qos);
    }

    public void unsubscribe(String topic) throws Exception {
        MqttAsyncClient client = factory.getClient();
        IMqttToken token = client.unsubscribe(topic);
        token.waitForCompletion();
        log.info("Unsubscribed from topic: {}", topic);
    }

    public void publish(String topic, String payload, int qos) throws Exception {
        MqttAsyncClient client = factory.getClient();
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(qos);
        client.publish(topic, message);
        if (publishCounter != null) {
            publishCounter.increment();
        }
        log.info("Published message to topic={} qos={}", topic, qos);
    }

    public CompletableFuture<Void> publishAsync(String topic, String payload, int qos) {
        return CompletableFuture.runAsync(() -> {
            try {
                publish(topic, payload, qos);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
