package zg.yoyo.localdeploy.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientLoadStatusLogger implements ApplicationRunner {

    private final Environment env;

    @Override
    public void run(ApplicationArguments args) {
        boolean minioReady = has("minio.endpoint") && has("minio.access-key") && has("minio.secret-key") && has("minio.bucket");
        if (minioReady) {
            log.info("MinIO client load status: ready");
        } else {
            log.info("MinIO client load status: skipped due to missing properties");
        }
        boolean emqxReady = has("spring.emqx.broker-url") && has("spring.emqx.client-id") && has("spring.emqx.username") && has("spring.emqx.password");
        if (emqxReady) {
            log.info("EMQX client load status: ready");
        } else {
            log.info("EMQX client load status: skipped due to missing properties");
        }
    }

    private boolean has(String key) {
        return env.getProperty(key) != null && !env.getProperty(key).isEmpty();
    }
}
