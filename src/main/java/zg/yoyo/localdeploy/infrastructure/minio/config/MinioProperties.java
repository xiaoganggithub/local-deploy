package zg.yoyo.localdeploy.infrastructure.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author zhenggang
 */
@Data
@ConfigurationProperties(prefix = "minio")
@Validated
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private Lifecycle lifecycle = new Lifecycle();

    public boolean isConfigured() {
        return notEmpty(endpoint) && notEmpty(accessKey) && notEmpty(secretKey) && notEmpty(bucket);
    }

    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    @Data
    public static class Lifecycle {
        private Integer defaultDays = 7;
        private Boolean enabled = true;
    }

    public void validateOrThrow() {
        if (!isConfigured()) {
            throw new IllegalArgumentException("MinIO配置不完整：需要配置endpoint、accessKey、secretKey、bucket");
        }
        if (lifecycle != null && lifecycle.getEnabled() != null && lifecycle.getEnabled()) {
            Integer days = lifecycle.getDefaultDays();
            if (days == null || days < 1) {
                throw new IllegalArgumentException("MinIO生命周期配置不合法：defaultDays必须为>=1的整数");
            }
        }
    }
}
