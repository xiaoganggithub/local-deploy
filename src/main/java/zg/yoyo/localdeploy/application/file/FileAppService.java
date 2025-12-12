package zg.yoyo.localdeploy.application.file;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zg.yoyo.localdeploy.domain.file.FileMeta;
import zg.yoyo.localdeploy.domain.file.FileMetaRepository;
import zg.yoyo.localdeploy.infrastructure.minio.MinioGateway;

/**
 * @author zhenggang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAppService {

    private final FileMetaRepository repo;
    private final MinioGateway minioGateway;

    @Value("${minio.bucket}")
    private String bucket;

    public String upload(MultipartFile file) throws Exception {
        log.info("Start uploading file, filename: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            log.error("Upload failed: empty file");
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String bizId = repo.nextBizId();
        String objectKey = bizId + "_" + file.getOriginalFilename();
        
        try {
            log.info("Start uploading to MinIO, bucket: {}, objectKey: {}", bucket, objectKey);
            minioGateway.putObject(bucket, objectKey, file);
            log.info("MinIO upload success, bucket: {}, objectKey: {}", bucket, objectKey);
            
            FileMeta meta = new FileMeta(bizId, file.getOriginalFilename(),
                    file.getContentType(), file.getSize(), bucket, objectKey);
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
        return minioGateway.getObject(meta.getBucket(), meta.getObjectKey());
    }

    public void delete(String bizId) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            return;
        }
        minioGateway.removeObject(meta.getBucket(), meta.getObjectKey());
        repo.deleteByBizId(bizId);
    }

    public String presignedUrl(String bizId, int expireMinutes) throws Exception {
        FileMeta meta = repo.findByBizId(bizId);
        if (meta == null) {
            throw new IllegalArgumentException("文件不存在");
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
        byte[] data = minioGateway.getObject(meta.getBucket(), meta.getObjectKey());
        String newObjectKey = bizId + "_" + targetName;
        minioGateway.putObject(meta.getBucket(), newObjectKey, data, meta.getContentType());
        minioGateway.removeObject(meta.getBucket(), meta.getObjectKey());
        repo.deleteByBizId(bizId);
        FileMeta updated = new FileMeta(meta.getBizId(), targetName, meta.getContentType(),
                meta.getSizeBytes(), meta.getBucket(), newObjectKey);
        repo.save(updated);
    }
}
