package zg.yoyo.localdeploy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import zg.yoyo.localdeploy.infrastructure.emqx.service.EmqxService;
import zg.yoyo.localdeploy.infrastructure.emqx.listener.MessageListener;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.emqx.broker-url=tcp://127.0.0.1:1883",
        "spring.emqx.client-id=test-client",
        "spring.emqx.username=admin",
        "spring.emqx.password=public",
        "spring.emqx.topic=app/messages"
})
public class MessageFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MessageListener messageListener;
    @MockBean
    private EmqxService emqxService;

    @Test
    void publishEndpoint_callsService() throws Exception {
        String body = "{\"topic\":\"app/messages\",\"qos\":1,\"message\":{\"id\":\"1\",\"type\":\"demo\",\"data\":{\"k\":\"v\"}}}";
        doNothing().when(emqxService).send(eq("app/messages"), eq("{\"id\":\"1\",\"type\":\"demo\",\"data\":{\"k\":\"v\"}}"), anyInt());
        mockMvc.perform(post("/api/messages/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(emqxService).send(topicCaptor.capture(), eq("{\"id\":\"1\",\"type\":\"demo\",\"data\":{\"k\":\"v\"}}"), eq(1));
    }

    @Test
    void listener_parsesMessage() throws Exception {
        String json = "{\"id\":\"1\",\"type\":\"demo\",\"data\":{\"x\":1}}";
        MqttMessage msg = new MqttMessage(json.getBytes());
        messageListener.onMessage("app/messages", msg);
    }
}
