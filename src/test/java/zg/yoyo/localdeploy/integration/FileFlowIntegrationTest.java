package zg.yoyo.localdeploy.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.minio.MinioClient;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import zg.yoyo.localdeploy.domain.file.FileMeta;
import zg.yoyo.localdeploy.domain.file.FileMetaRepository;
import zg.yoyo.localdeploy.infrastructure.minio.MinioGateway;
import zg.yoyo.localdeploy.infrastructure.minio.MinioLifecycleService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "minio.endpoint=http://127.0.0.1:9000",
        "minio.access-key=ak",
        "minio.secret-key=sk",
        "minio.bucket=poc-bucket",
        "minio.lifecycle.enabled=true",
        "minio.lifecycle.default-days=7"
})
public class FileFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MinioClient minioClient;

    @SpyBean
    private MinioGateway minioGateway;

    @SpyBean
    private MinioLifecycleService lifecycleService;

    @MockBean
    private FileMetaRepository fileMetaRepository;

    @Test
    void uploadFlow_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));
        doNothing().when(lifecycleService).ensurePrefixRule(any(String.class), any(String.class), any(Integer.class));
        doReturn("ttl/7d/123_hello.txt").when(minioGateway)
                .putObject(eq("poc-bucket"), any(String.class), any(MultipartFile.class), org.mockito.ArgumentMatchers.anyMap());

        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("ttl", "7d")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        ArgumentCaptor<FileMeta> captor = ArgumentCaptor.forClass(FileMeta.class);
        verify(fileMetaRepository).save(captor.capture());
    }

    @Test
    void presigned_success() throws Exception {
        FileMeta meta = new FileMeta("biz", "hello.txt", "text/plain", 5L,
                "poc-bucket", "ttl/7d/biz_hello.txt", java.time.OffsetDateTime.now().plusDays(7), false);
        org.mockito.Mockito.doReturn(meta).when(fileMetaRepository).findByBizId(eq("biz"));
        org.mockito.Mockito.doReturn("http://minio/presigned").when(minioGateway)
                .presignedGet(eq("poc-bucket"), eq("ttl/7d/biz_hello.txt"), eq(10));

        mockMvc.perform(get("/api/files/biz/presigned").param("minutes", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void download_success() throws Exception {
        FileMeta meta = new FileMeta("biz", "hello.txt", "text/plain", 5L,
                "poc-bucket", "ttl/7d/biz_hello.txt", java.time.OffsetDateTime.now().plusDays(7), false);
        org.mockito.Mockito.doReturn(meta).when(fileMetaRepository).findByBizId(eq("biz"));
        org.mockito.Mockito.doReturn("bytes".getBytes(StandardCharsets.UTF_8)).when(minioGateway)
                .getObject(eq("poc-bucket"), eq("ttl/7d/biz_hello.txt"));

        mockMvc.perform(get("/api/files/biz"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_success() throws Exception {
        FileMeta meta = new FileMeta("biz", "hello.txt", "text/plain", 5L,
                "poc-bucket", "ttl/7d/biz_hello.txt", java.time.OffsetDateTime.now().plusDays(7), false);
        org.mockito.Mockito.doReturn(meta).when(fileMetaRepository).findByBizId(eq("biz"));
        org.mockito.Mockito.doNothing().when(minioGateway).removeObject(eq("poc-bucket"), eq("ttl/7d/biz_hello.txt"));

        mockMvc.perform(delete("/api/files/biz"))
                .andExpect(status().isOk());
        verify(fileMetaRepository).deleteByBizId(eq("biz"));
    }
}
