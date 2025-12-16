# Local Deploy

基于 Spring Boot 3 的本地部署服务平台，集成文件存储、消息代理等基础设施能力。

## 技术栈

- **Java 17** + **Spring Boot 3.3.5**
- **PostgreSQL** + MyBatis-Plus + Druid 连接池
- **MinIO** - 对象存储，支持生命周期管理
- **EMQX** - MQTT 消息代理
- **Hutool** - 工具库

## 项目结构

```
src/main/java/zg/yoyo/localdeploy/
├── application/       # 应用服务层
├── domain/           # 领域模型
├── infrastructure/   # 基础设施层
│   ├── config/       # 配置类
│   ├── emqx/         # EMQX 消息代理
│   ├── minio/        # MinIO 对象存储
│   └── persistence/  # 持久化
└── interfaces/       # 接口层
    ├── file/         # 文件接口
    └── message/      # 消息接口
```

## 核心功能

### 1. 文件存储 (MinIO)

- 文件上传/下载
- 生命周期管理 - 支持按 TTL 自动过期删除
- 上传接口：`POST /api/files`，参数 `ttl`（如 `7d`）

### 2. 消息代理 (EMQX)

- MQTT 消息发布/订阅
- 自动重连机制
- 多主题支持

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL
- MinIO
- EMQX

### 启动依赖服务

```bash
# PostgreSQL
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=root postgres:latest

# MinIO
docker run -d --name minio -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=myadmin -e MINIO_ROOT_PASSWORD=myadmin123 \
  minio/minio server /data --console-address ":9001"

# EMQX
docker run -d --name emqx -p 1883:1883 -p 18083:18083 emqx/emqx:latest
```

### 配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/postgres
    username: postgres
    password: root

minio:
  endpoint: http://127.0.0.1:9000
  access-key: myadmin
  secret-key: myadmin123
  bucket: poc-bucket

spring.emqx:
  broker-url: tcp://127.0.0.1:1883
  username: admin
  password: public
```

### 构建运行

```bash
# 构建
mvn clean package -DskipTests

# 运行
java -jar target/local-deploy-0.0.1-SNAPSHOT.jar
```

服务启动后访问：`http://localhost:9099`

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/files | 上传文件（支持 TTL） |
| GET | /api/files/{bizId} | 下载文件 |

## 文档

- [EMQX 消息代理演示](README_EMQX_DEMO.md)
- [MinIO 生命周期管理](README_MINIO_LIFECYCLE.md)

## License

MIT
