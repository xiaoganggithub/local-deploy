package zg.yoyo.localdeploy.domain.file;

import lombok.Getter;
import lombok.ToString;

/**
 * 文件元数据聚合根。
 */
@Getter
@ToString
public class FileMeta {
    private final String bizId;
    private final String filename;
    private final String contentType;
    private final long sizeBytes;
    private final String bucket;
    private final String objectKey;

    public FileMeta(String bizId, String filename, String contentType, long sizeBytes,
                    String bucket, String objectKey) {
        this.bizId = bizId;
        this.filename = filename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.bucket = bucket;
        this.objectKey = objectKey;
    }
}
