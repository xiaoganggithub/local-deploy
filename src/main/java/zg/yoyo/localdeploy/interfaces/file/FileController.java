package zg.yoyo.localdeploy.interfaces.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zg.yoyo.localdeploy.application.file.FileAppService;
import zg.yoyo.localdeploy.interfaces.common.ApiResponse;

/**
 * @author zhenggang
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileAppService appService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String bizId = appService.upload(file);
        return ApiResponse.success(bizId);
    }

    @GetMapping("/{bizId}")
    public ResponseEntity<byte[]> download(@PathVariable String bizId) throws Exception {
        byte[] data = appService.download(bizId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bizId + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/{bizId}")
    public ApiResponse<Void> delete(@PathVariable String bizId) throws Exception {
        appService.delete(bizId);
        return ApiResponse.success();
    }

    @GetMapping("/{bizId}/presigned")
    public ApiResponse<String> presigned(@PathVariable String bizId,
                            @RequestParam(defaultValue = "10") int minutes) throws Exception {
        String url = appService.presignedUrl(bizId, minutes);
        return ApiResponse.success(url);
    }

    @PutMapping("/{bizId}")
    public ApiResponse<Void> rename(@PathVariable String bizId, @RequestParam String filename) throws Exception {
        appService.updateFilename(bizId, filename);
        return ApiResponse.success();
    }
}
