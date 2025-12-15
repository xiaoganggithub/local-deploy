# MinIO 生命周期管理方案

## API 变更
- 上传接口：`POST /api/files`
  - 必填参数：`ttl`（ISO 持续时间的简化形式，仅按天），示例：`7d` 或 `P7D`
  - 表单字段：`file`（multipart）
  - 返回：业务ID（`bizId`）

## 规则设计
- 对象按前缀存储：`ttl/{Nd}/<bizId>_<filename>`
- 生命周期规则按前缀匹配：
  - 例如：`prefix=ttl/7d/`，`Expiration.Days=7`
- 动态规则：
  - 首次出现新的 `ttl` 值时，自动为该前缀添加规则

## 配置项
- `minio.lifecycle.enabled`：启用/关闭生命周期管理（默认开启）
- `minio.lifecycle.default-days`：未使用

## 验证与监控
- 测试不同 `ttl`（如 `1d`、`7d`、`30d`）
- 验证规则生效与对象删除时间的一致性
- 监控规则配置日志与删除审计

## 最佳实践
- 仅使用按天的 `ttl` 值
- 重要数据使用独立桶或不加前缀
- 需要二次确认时采用演练模式，仅生成待删清单
