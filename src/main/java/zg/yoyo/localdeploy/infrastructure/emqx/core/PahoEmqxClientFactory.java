package zg.yoyo.localdeploy.infrastructure.emqx.core;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxProperties;

/**
 * @author zhenggang
 */
@Slf4j
public class PahoEmqxClientFactory implements EmqxClientFactory {

    private final EmqxProperties props;
    private final MqttConnectOptions options;
    private volatile MqttAsyncClient client;

    public PahoEmqxClientFactory(EmqxProperties props, MqttConnectOptions options) {
        this.props = props;
        this.options = options;
    }

    @Override
    public synchronized MqttAsyncClient getClient() throws Exception {
        if (client == null) {
            client = new MqttAsyncClient(props.getBrokerUrl(), props.getClientId(), new MemoryPersistence());
        }
        if (!client.isConnected()) {
            IMqttToken token = client.connect(options);
            token.waitForCompletion();
            log.info("EMQX connected: {}", props.getBrokerUrl());
        }
        return client;
    }

    @Override
    public synchronized void close() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception e) {
            log.warn("Error closing EMQX client: {}", e.getMessage(), e);
        }
    }
}
