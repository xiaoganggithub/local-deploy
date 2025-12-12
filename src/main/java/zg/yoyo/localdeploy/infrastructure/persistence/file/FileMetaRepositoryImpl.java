package zg.yoyo.localdeploy.infrastructure.persistence.file;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zg.yoyo.localdeploy.domain.file.FileMeta;
import zg.yoyo.localdeploy.domain.file.FileMetaRepository;

@Repository
@RequiredArgsConstructor
public class FileMetaRepositoryImpl implements FileMetaRepository {

    private final FileMetaMapper mapper;

    @Override
    public String nextBizId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public void save(FileMeta meta) {
        FileMetaDO po = new FileMetaDO();
        po.setBizId(meta.getBizId());
        po.setFilename(meta.getFilename());
        po.setContentType(meta.getContentType());
        po.setSizeBytes(meta.getSizeBytes());
        po.setBucket(meta.getBucket());
        po.setObjectKey(meta.getObjectKey());
        mapper.insert(po);
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
                po.getSizeBytes(), po.getBucket(), po.getObjectKey());
    }

    @Override
    public void deleteByBizId(String bizId) {
        mapper.delete(new LambdaQueryWrapper<FileMetaDO>().eq(FileMetaDO::getBizId, bizId));
    }
}
