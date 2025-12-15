package zg.yoyo.localdeploy.infrastructure.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioConfiguredCondition;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioProperties;

@Configuration
@Slf4j
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    @org.springframework.context.annotation.Conditional(MinioConfiguredCondition.class)
    public MinioClient minioClient(MinioProperties props) {
        MinioClient client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
        log.info("MinIO client initialized: endpoint={}", props.getEndpoint());
        return client;
    }

    // All MinIO related configurations are centralized in MinioProperties
}
