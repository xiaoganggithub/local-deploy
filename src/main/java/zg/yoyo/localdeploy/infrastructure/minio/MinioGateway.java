package zg.yoyo.localdeploy.infrastructure.minio;

import cn.hutool.core.io.IoUtil;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioGateway {

    private final MinioClient minioClient;

    public void ensureBucket(String bucket) throws Exception {
        log.info("Checking if bucket exists: {}", bucket);
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                log.info("Bucket doesn't exist, creating: {}", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket created successfully: {}", bucket);
            } else {
                log.info("Bucket already exists: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to check or create bucket, bucket: {}, error: {}", bucket, e.getMessage(), e);
            throw e;
        }
    }

    public String putObject(String bucket, String objectKey, MultipartFile file) throws Exception {
        log.info("Start MinIO putObject operation, bucket: {}, objectKey: {}", bucket, objectKey);
        ensureBucket(bucket);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("MinIO putObject operation success, bucket: {}, objectKey: {}", bucket, objectKey);
        } catch (Exception e) {
            log.error("MinIO putObject operation failed, bucket: {}, objectKey: {}, error: {}", bucket, objectKey, e.getMessage(), e);
            throw e;
        }
        return objectKey;
    }

    public String putObject(String bucket, String objectKey, byte[] data, String contentType) throws Exception {
        ensureBucket(bucket);
        try (InputStream in = IoUtil.toStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, data.length, -1)
                            .contentType(contentType)
                            .build());
        }
        return objectKey;
    }

    public byte[] getObject(String bucket, String objectKey) throws Exception {
        try (GetObjectResponse resp = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
            return IoUtil.readBytes(resp);
        }
    }

    public void removeObject(String bucket, String objectKey) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    }

    public String presignedGet(String bucket, String objectKey, int expireMinutes) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .method(Method.GET)
                        .expiry(expireMinutes, TimeUnit.MINUTES)
                        .build());
    }
}
