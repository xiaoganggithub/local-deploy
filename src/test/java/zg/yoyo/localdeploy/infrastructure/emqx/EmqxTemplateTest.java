package zg.yoyo.localdeploy.infrastructure.emqx;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.junit.jupiter.api.Test;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxProperties;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxClientFactory;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxTemplate;

public class EmqxTemplateTest {

    @Test
    void publishAndSubscribe() throws Exception {
        MqttAsyncClient client = mock(MqttAsyncClient.class);
        IMqttToken token = mock(IMqttToken.class);
        when(client.isConnected()).thenReturn(true);
        when(client.subscribe(any(String.class), any(Integer.class), any())).thenReturn(token);

        EmqxClientFactory factory = new EmqxClientFactory() {
            @Override
            public MqttAsyncClient getClient() {
                return client;
            }
            @Override
            public void close() {}
        };
        EmqxProperties props = new EmqxProperties();
        props.setBrokerUrl("tcp://localhost:1883");
        props.setClientId("cid");
        EmqxTemplate template = new EmqxTemplate(factory, props, null);
        assertTrue(template.isConnected());
        template.subscribe("t", 1, (topic, message) -> {});
        template.publishAsync("t", "msg", 1).get();
    }
}
