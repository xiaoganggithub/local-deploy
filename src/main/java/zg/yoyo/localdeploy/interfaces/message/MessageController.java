package zg.yoyo.localdeploy.interfaces.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zg.yoyo.localdeploy.infrastructure.emqx.service.EmqxService;
import zg.yoyo.localdeploy.interfaces.common.ApiResponse;
import zg.yoyo.localdeploy.interfaces.message.dto.PublishRequest;

/**
 * @author zhenggang
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MessageController {

    private final EmqxService emqxService;
    private final ObjectMapper objectMapper;

    @PostMapping("/publish")
    public ApiResponse<Void> publish(@Valid @RequestBody PublishRequest req) throws Exception {
        String payload = objectMapper.writeValueAsString(req.getMessage());
        emqxService.send(req.getTopic(), payload, req.getQos());
        log.info("Message published, topic={}, qos={}", req.getTopic(), req.getQos());
        return ApiResponse.success();
    }
}
