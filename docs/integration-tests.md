# Integration Test Suite

## Scope
- Cover major business interactions across modules (file storage, messaging).
- Exercise cross-boundary interfaces via HTTP controllers and messaging endpoints.
- Validate data flow correctness for end-to-end scenarios.

## Test Cases

### FileFlowIntegrationTest
- Purpose: Verify file upload, download, delete, and presigned URL flows through controller, service, and MinIO gateway.
- Steps:
  1. Upload via `POST /api/files` with multipart and `ttl=7d`.
  2. Download via `GET /api/files/{bizId}`.
  3. Generate presigned URL via `GET /api/files/{bizId}/presigned?minutes=10`.
  4. Delete via `DELETE /api/files/{bizId}`.
- Expected Responses:
  - Upload returns 200 with `bizId`.
  - Download returns 200 and file bytes.
  - Presigned returns 200 and a URL string.
  - Delete returns 200.
- Data Prep:
  - Test properties provide MinIO configuration.
  - Repository mocked; MinIO client stubbed via spies.
- Environment:
  - Spring Boot test context.
  - No external MinIO dependency; interactions are stubbed.

### MessageFlowIntegrationTest
- Purpose: Verify message publishing via REST and listener consumption path.
- Steps:
  1. Publish via `POST /api/messages/publish` with JSON body containing topic, qos, message DTO.
  2. Invoke listener with a JSON payload.
- Expected Responses:
  - Publish returns 200; service called with topic and serialized JSON.
  - Listener logs deserialized DTO without errors.
- Data Prep:
  - EMQX properties provided via test properties.
  - Messaging service mocked.
- Environment:
  - Spring Boot test context; no external broker required.

## Technology
- JUnit 5 with Spring Boot testing.
- Mockito for mocks/stubs/spies.
- MockMvc for REST API exercising.
- Surefire plugin generates XML and text reports under `target/surefire-reports/`.

## Execution
- Run all tests: `./mvnw.cmd test`
- CI/CD:
  - Integrate with Maven Surefire in pipeline.
  - Artifacts: `target/surefire-reports/*.xml` for reporting tools.

## Quality
- Core business flows covered end-to-end:
  - File storage controller/service/gateway interactions.
  - Message publish controller and listener parsing.
- Deterministic, repeatable tests with mocked external boundaries.
