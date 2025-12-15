package zg.yoyo.localdeploy.domain.file;

import java.util.List;

/**
 * 文件元数据仓储接口。
 */
public interface FileMetaRepository {
    String nextBizId();

    void save(FileMeta meta);

    FileMeta findByBizId(String bizId);

    void deleteByBizId(String bizId);

    List<FileMeta> findExpiredFiles(int limit);
}
