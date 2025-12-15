package zg.yoyo.localdeploy.interfaces.message.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublishRequest {
    @NotBlank
    private String topic;
    @Min(0)
    private int qos = 1;
    @Valid
    private MessageDto message;
}
