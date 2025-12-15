package zg.yoyo.localdeploy.infrastructure.emqx.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "spring.emqx")
public class EmqxProperties {

    @NotBlank
    private String brokerUrl = "tcp://127.0.0.1:1883";

    @NotBlank
    private String username = "admin";

    @NotBlank
    private String password = "public";

    @NotBlank
    private String clientId = "emqx-client";

    @Min(0)
    private int qos = 1;

    private boolean ssl = false;

    @Min(1)
    private int poolSize = 1;

    @Min(1)
    private int reconnectDelaySeconds = 5;

    private List<String> defaultSubscribeTopics;
}
