package zg.yoyo.localdeploy.infrastructure.minio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.MinioClient;
import io.minio.GetBucketLifecycleArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioProperties;

public class MinioLifecycleServiceTest {

    @Test
    void addRuleWhenNoExistingRule() throws Exception {
        MinioClient client = mock(MinioClient.class);
        MinioProperties props = new MinioProperties();
        props.setEndpoint("http://localhost:9000");
        props.setAccessKey("ak");
        props.setSecretKey("sk");
        props.setBucket("b");
        props.getLifecycle().setEnabled(true);
        when(client.getBucketLifecycle(any(GetBucketLifecycleArgs.class)))
                .thenReturn(null);

        MinioLifecycleService service = new MinioLifecycleService(client, props);

        assertDoesNotThrow(() -> service.ensurePrefixRule("bucket", "ttl/7d/", 7));
        verify(client, times(1)).setBucketLifecycle(any(SetBucketLifecycleArgs.class));
    }

    @Test
    void skipAddingWhenMatchingRuleExists() throws Exception {
        MinioClient client = mock(MinioClient.class);
        MinioProperties props = new MinioProperties();
        props.setEndpoint("http://localhost:9000");
        props.setAccessKey("ak");
        props.setSecretKey("sk");
        props.setBucket("b");
        props.getLifecycle().setEnabled(true);

        String prefix = "ttl/7d/";
        int days = 7;
        LifecycleRule existingRule = new LifecycleRule(
                Status.ENABLED,
                null,
                new Expiration((java.time.ZonedDateTime) null, days, null),
                new RuleFilter(prefix),
                "expire-7d-" + Integer.toHexString(prefix.hashCode()),
                null,
                null,
                null
        );
        LifecycleConfiguration existingCfg = new LifecycleConfiguration(List.of(existingRule));
        when(client.getBucketLifecycle(any(GetBucketLifecycleArgs.class))).thenReturn(existingCfg);

        MinioLifecycleService service = new MinioLifecycleService(client, props);

        assertDoesNotThrow(() -> service.ensurePrefixRule("bucket", prefix, days));
        verify(client, times(0)).setBucketLifecycle(any(SetBucketLifecycleArgs.class));
    }
}
