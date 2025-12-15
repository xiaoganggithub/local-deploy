package zg.yoyo.localdeploy.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import zg.yoyo.localdeploy.infrastructure.minio.MinioGateway;
import zg.yoyo.localdeploy.infrastructure.minio.MinioLifecycleService;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioProperties;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(io.minio.MinioClient.class)
public class MinioInitializationRunner implements CommandLineRunner {

    private final MinioGateway minioGateway;
    private final MinioLifecycleService lifecycleService;
    private final MinioProperties minioProperties;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing MinIO configuration...");
        try {
            String bucket = minioProperties.getBucket();
            minioProperties.validateOrThrow();
            // Ensure bucket exists first
            minioGateway.ensureBucket(bucket);
            
            // Configure lifecycle
            lifecycleService.configureBucketLifecycle(bucket);
            
        } catch (Exception e) {
            log.error("Failed to initialize MinIO: {}", e.getMessage(), e);
            // Don't fail the app startup, just log error
        }
    }
}
