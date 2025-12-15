package zg.yoyo.localdeploy.infrastructure.persistence.file;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author zhenggang
 */
@Data
@TableName("file_meta")
public class FileMetaDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizId;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private String bucket;
    private String objectKey;
    private OffsetDateTime expireAt;
    private Boolean deleted;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
