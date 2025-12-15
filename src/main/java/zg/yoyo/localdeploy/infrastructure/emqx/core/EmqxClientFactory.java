package zg.yoyo.localdeploy.infrastructure.emqx.core;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;

public interface EmqxClientFactory {
    MqttAsyncClient getClient() throws Exception;
    void close();
}
