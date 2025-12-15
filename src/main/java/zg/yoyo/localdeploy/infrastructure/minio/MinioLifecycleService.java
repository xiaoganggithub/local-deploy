package zg.yoyo.localdeploy.infrastructure.minio;

import io.minio.MinioClient;
import io.minio.GetBucketLifecycleArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhenggang
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(MinioClient.class)
public class MinioLifecycleService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public void configureBucketLifecycle(String bucket) {
        Boolean enabled = minioProperties.getLifecycle() != null ? minioProperties.getLifecycle().getEnabled() : Boolean.TRUE;
        if (enabled == null || !enabled) {
            log.info("MinIO lifecycle management is disabled.");
            return;
        }

        log.info("MinIO lifecycle enabled for bucket: {}", bucket);
    }

    public void ensurePrefixRule(String bucket, String prefix, int days) {
        Boolean enabled = minioProperties.getLifecycle() != null ? minioProperties.getLifecycle().getEnabled() : Boolean.TRUE;
        if (enabled == null || !enabled) {
            return;
        }
        try {
            LifecycleConfiguration existing = minioClient.getBucketLifecycle(
                    GetBucketLifecycleArgs.builder().bucket(bucket).build());
            List<LifecycleRule> rules = existing != null && existing.rules() != null
                    ? new ArrayList<>(existing.rules()) : new ArrayList<>();
            for (LifecycleRule r : rules) {
                RuleFilter f = r.filter();
                if (f != null && Objects.equals(f.prefix(), prefix)) {
                    Expiration exp = r.expiration();
                    if (exp != null && exp.days() != null && exp.days() == days && r.status() == Status.ENABLED) {
                        return;
                    }
                }
            }
            String id = "expire-" + days + "d-" + Integer.toHexString(prefix.hashCode());
            LifecycleRule rule = new LifecycleRule(
                    Status.ENABLED,
                    null,
                    new Expiration((java.time.ZonedDateTime) null, days, null),
                    new RuleFilter(prefix),
                    id,
                    null,
                    null,
                    null);
            rules.add(rule);
            LifecycleConfiguration cfg = new LifecycleConfiguration(rules);
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(bucket)
                            .config(cfg)
                            .build());
            log.info("Added lifecycle rule: bucket={}, prefix={}, days={}", bucket, prefix, days);
        } catch (Exception e) {
            log.error("ensurePrefixRule failed: {}", e.getMessage(), e);
        }
    }
    
    public void removeLifecycleConfiguration(String bucket) {
        try {
            minioClient.deleteBucketLifecycle(
                io.minio.DeleteBucketLifecycleArgs.builder().bucket(bucket).build()
            );
            log.info("Removed lifecycle configuration for bucket: {}", bucket);
        } catch (Exception e) {
             log.error("Failed to remove lifecycle configuration: {}", e.getMessage(), e);
        }
    }
}
