package zg.yoyo.localdeploy.interfaces.message.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;

/**
 * @author zhenggang
 */
@Data
public class MessageDto {
    @NotBlank
    private String id;
    @NotBlank
    private String type;
    private Map<String, Object> data;
}
