package zg.yoyo.localdeploy.infrastructure.persistence.file;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zg.yoyo.localdeploy.domain.file.FileMeta;
import zg.yoyo.localdeploy.domain.file.FileMetaRepository;

/**
 * @author zhenggang
 */
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FileMetaRepositoryImpl implements FileMetaRepository {

    private final FileMetaMapper mapper;

    @Override
    public List<FileMeta> findExpiredFiles(int limit) {
        // Find files that:
        // 1. Have expireAt < NOW
        // 2. Are NOT marked as deleted
        // 3. Limit result
        
        List<FileMetaDO> pos = mapper.selectList(new LambdaQueryWrapper<FileMetaDO>()
                .lt(FileMetaDO::getExpireAt, OffsetDateTime.now())
                .eq(FileMetaDO::getDeleted, false)
                .last("limit " + limit));
                
        return pos.stream().map(po -> new FileMeta(
                po.getBizId(), po.getFilename(), po.getContentType(),
                po.getSizeBytes(), po.getBucket(), po.getObjectKey(), po.getExpireAt(), 
                Boolean.TRUE.equals(po.getDeleted())
        )).collect(Collectors.toList());
    }

    @Override
    public String nextBizId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public void save(FileMeta meta) {
        // Check if exists update, else insert. But domain is aggregate root, usually we just save.
        // For simplicity, let's check if bizId exists.
        FileMetaDO existing = mapper.selectOne(new LambdaQueryWrapper<FileMetaDO>()
                .eq(FileMetaDO::getBizId, meta.getBizId())
                .last("limit 1"));
                
        if (existing != null) {
            existing.setFilename(meta.getFilename());
            existing.setContentType(meta.getContentType());
            existing.setSizeBytes(meta.getSizeBytes());
            existing.setBucket(meta.getBucket());
            existing.setObjectKey(meta.getObjectKey());
            existing.setExpireAt(meta.getExpireAt());
            existing.setDeleted(meta.isDeleted());
            mapper.updateById(existing);
        } else {
            FileMetaDO po = new FileMetaDO();
            po.setBizId(meta.getBizId());
            po.setFilename(meta.getFilename());
            po.setContentType(meta.getContentType());
            po.setSizeBytes(meta.getSizeBytes());
            po.setBucket(meta.getBucket());
            po.setObjectKey(meta.getObjectKey());
            po.setExpireAt(meta.getExpireAt());
            po.setDeleted(meta.isDeleted());
            mapper.insert(po);
        }
    }

    @Override
    public FileMeta findByBizId(String bizId) {
        FileMetaDO po = mapper.selectOne(new LambdaQueryWrapper<FileMetaDO>()
                .eq(FileMetaDO::getBizId, bizId)
                .last("limit 1"));
        if (po == null) {
            return null;
        }
        return new FileMeta(po.getBizId(), po.getFilename(), po.getContentType(),
                po.getSizeBytes(), po.getBucket(), po.getObjectKey(), po.getExpireAt(), 
                Boolean.TRUE.equals(po.getDeleted()));
    }

    @Override
    public void deleteByBizId(String bizId) {
        mapper.delete(new LambdaQueryWrapper<FileMetaDO>().eq(FileMetaDO::getBizId, bizId));
    }
}
