package zg.yoyo.localdeploy.interfaces.common;

import lombok.Data;

/**
 * 统一API响应实体
 *
 * @author zhenggang
 */
@Data
public class ApiResponse<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    /**
     * 错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 错误响应（默认错误码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}