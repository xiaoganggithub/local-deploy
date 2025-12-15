package zg.yoyo.localdeploy.application.file;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import zg.yoyo.localdeploy.domain.file.FileMeta;
import zg.yoyo.localdeploy.domain.file.FileMetaRepository;
import zg.yoyo.localdeploy.infrastructure.minio.MinioGateway;
import zg.yoyo.localdeploy.infrastructure.minio.MinioLifecycleService;
import zg.yoyo.localdeploy.infrastructure.minio.config.MinioProperties;

/**
 * @author zhenggang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAppService {

    private final FileMetaRepository repo;
    private final ObjectProvider<MinioGateway> minioGatewayProvider;
    private final ObjectProvider<MinioLifecycleService> lifecycleServiceProvider;
    private final MinioProperties minioProperties;

    private String bucket() {
        return minioProperties.getBucket();
    }

    public String upload(MultipartFile file, String ttl) throws Exception {
        log.info("Start uploading file, filename: {}, ttl: {}", file.getOriginalFilename(), ttl);
        
        if (file.isEmpty()) {
            log.error("Upload failed: empty file");
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String bizId = repo.nextBizId();
        int days = parseDays(ttl);
        String ttlNorm = days + "d";
        String prefix = "ttl/" + ttlNorm + "/";
        String objectKey = prefix + bizId + "_" + file.getOriginalFilename();

        OffsetDateTime expireAt = OffsetDateTime.now().plusDays(days);
        
        try {
            MinioLifecycleService lifecycleService = lifecycleServiceProvider.getIfAvailable();
            MinioGateway minioGateway = minioGatewayProvider.getIfAvailable();
            String bucket = bucket();
            if (minioGateway == null || lifecycleService == null || bucket == null || bucket.isEmpty()) {
                log.warn("MinIO client not initialized or bucket not configured, skipping upload");
                throw new IllegalStateException("MinIO未配置或缺少必要属性，客户端未初始化");
            }
            lifecycleService.ensurePrefixRule(bucket, prefix, days);
            log.info("Start uploading to MinIO, bucket: {}, objectKey: {}, ttl: {}", bucket, objectKey, ttlNorm);
            
            minioGateway.putObject(bucket, objectKey, file, Map.of("ttl", ttlNorm));
            
            log.info("MinIO upload success, bucket: {}, objectKey: {}", bucket, objectKey);
            
            FileMeta meta = new FileMeta(bizId, file.getOriginalFilename(),
                    file.getContentType(), file.getSize(), bucket, objectKey, expireAt, false);
            repo.save(meta);
            log.info("File metadata saved successfully, bizId: {}", bizId);
            
            return bizId;
        } catch (Exception e) {
            log.error("File upload failed, filename: {}, error: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }
    }

    public byte[] download(String bizId) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        MinioGateway minioGateway = minioGatewayProvider.getIfAvailable();
        if (minioGateway == null) {
            log.warn("MinIO client not initialized, skipping download");
            throw new IllegalStateException("MinIO未配置或缺少必要属性，客户端未初始化");
        }
        return minioGateway.getObject(meta.getBucket(), meta.getObjectKey());
    }

    public void delete(String bizId) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            return;
        }
        MinioGateway minioGateway = minioGatewayProvider.getIfAvailable();
        if (minioGateway == null) {
            log.warn("MinIO client not initialized, skipping delete");
            throw new IllegalStateException("MinIO未配置或缺少必要属性，客户端未初始化");
        }
        minioGateway.removeObject(meta.getBucket(), meta.getObjectKey());
        repo.deleteByBizId(bizId);
        log.info("File deleted successfully, bizId: {}", bizId);
    }

    public String presignedUrl(String bizId, int expireMinutes) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        MinioGateway minioGateway = minioGatewayProvider.getIfAvailable();
        if (minioGateway == null) {
            log.warn("MinIO client not initialized, skipping presigned url");
            throw new IllegalStateException("MinIO未配置或缺少必要属性，客户端未初始化");
        }
        return minioGateway.presignedGet(meta.getBucket(), meta.getObjectKey(),
                Math.max(expireMinutes, 1));
    }

    public void updateFilename(String bizId, String newName) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        String targetName = StrUtil.emptyToDefault(newName, meta.getFilename());
        if (targetName.equals(meta.getFilename())) {
            return;
        }
        MinioGateway minioGateway = minioGatewayProvider.getIfAvailable();
        if (minioGateway == null) {
            log.warn("MinIO client not initialized, skipping rename");
            throw new IllegalStateException("MinIO未配置或缺少必要属性，客户端未初始化");
        }
        byte[] data = minioGateway.getObject(meta.getBucket(), meta.getObjectKey());
        String newObjectKey = bizId + "_" + targetName;
        minioGateway.putObject(meta.getBucket(), newObjectKey, data, meta.getContentType());
        minioGateway.removeObject(meta.getBucket(), meta.getObjectKey());
        repo.deleteByBizId(bizId);
        FileMeta updated = new FileMeta(meta.getBizId(), targetName, meta.getContentType(),
                meta.getSizeBytes(), meta.getBucket(), newObjectKey, meta.getExpireAt(), false);
        repo.save(updated);
    }

    private int parseDays(String ttl) {
        if (ttl == null || ttl.isEmpty()) {
            throw new IllegalArgumentException("ttl参数不能为空");
        }
        String s = ttl.trim().toLowerCase();
        if (s.startsWith("p") && s.endsWith("d")) {
            try {
                String num = s.substring(1, s.length() - 1);
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ttl格式不正确，示例：P7D或7d");
            }
        }
        if (s.endsWith("d")) {
            try {
                String num = s.substring(0, s.length() - 1);
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ttl格式不正确，示例：P7D或7d");
            }
        }
        throw new IllegalArgumentException("仅支持按天设置ttl，示例：P7D或7d");
    }
}
