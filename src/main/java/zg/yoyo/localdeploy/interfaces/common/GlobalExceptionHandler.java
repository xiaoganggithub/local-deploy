package zg.yoyo.localdeploy.interfaces.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author zhenggang
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理IllegalArgumentException异常
     *
     * @param e 异常对象
     * @return 统一响应实体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 处理RuntimeException异常
     *
     * @param e 异常对象
     * @return 统一响应实体
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "内部服务器错误");
    }

    /**
     * 处理Exception异常
     *
     * @param e 异常对象
     * @return 统一响应实体
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "内部服务器错误");
    }
}