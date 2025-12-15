package zg.yoyo.localdeploy.interfaces.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import zg.yoyo.localdeploy.infrastructure.emqx.service.EmqxService;
import zg.yoyo.localdeploy.interfaces.message.dto.MessageDto;
import zg.yoyo.localdeploy.interfaces.message.dto.PublishRequest;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private EmqxService emqxService;

    @Test
    void publishOk() throws Exception {
        MessageDto dto = new MessageDto();
        dto.setId("1");
        dto.setType("test");
        PublishRequest req = new PublishRequest();
        req.setTopic("app/messages");
        req.setQos(1);
        req.setMessage(dto);
        doNothing().when(emqxService).send(anyString(), anyString(), anyInt());
        mockMvc.perform(post("/api/messages/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void publishBadRequest() throws Exception {
        String payload = "{\"qos\":1,\"message\":{\"id\":\"1\",\"type\":\"t\"}}";
        mockMvc.perform(post("/api/messages/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
