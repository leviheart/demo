package tw_six.demo.common;

import java.time.LocalDateTime;

/**
 * API统一响应封装类 - 标准化接口返回格式
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供统一的API响应格式，确保所有接口返回数据结构一致。
 * 前端可以根据统一的格式解析响应，简化错误处理和数据处理逻辑。
 * 
 * 【响应结构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ ApiResponse<T>                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ success: boolean     - 操作是否成功                                     │
 * │ message: String      - 响应消息（成功/错误描述）                         │
 * │ data: T              - 响应数据（泛型，可以是任意类型）                  │
 * │ code: int            - 状态码（200成功，4xx/5xx错误）                   │
 * │ timestamp: LocalDateTime - 响应时间戳                                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【JSON示例】
 * 成功响应：
 * ```json
 * {
 *   "success": true,
 *   "message": "操作成功",
 *   "data": { "id": 1, "name": "车辆A" },
 *   "code": 200,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * ```
 * 
 * 错误响应：
 * ```json
 * {
 *   "success": false,
 *   "message": "车辆不存在",
 *   "data": null,
 *   "code": 404,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * ```
 * 
 * 【状态码规范】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 状态码 │ 含义                          │ 使用场景                      │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ 200    │ 成功                          │ 请求处理成功                  │
 * │ 400    │ 请求参数错误                  │ 参数校验失败                  │
 * │ 401    │ 未授权                        │ 未登录或Token过期             │
 * │ 403    │ 禁止访问                      │ 无权限访问                    │
 * │ 404    │ 资源不存在                    │ 查询数据不存在                │
 * │ 500    │ 服务器内部错误                │ 系统异常                      │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【泛型说明】
 * 使用泛型T，data字段可以是任意类型：
 * - 单个对象：ApiResponse<User>
 * - 列表：ApiResponse<List<User>>
 * - 分页数据：ApiResponse<PageResult<User>>
 * 
 * 【使用示例】
 * ```java
 * // 成功响应
 * return ResponseEntity.ok(ApiResponse.success(user));
 * 
 * // 成功响应（自定义消息）
 * return ResponseEntity.ok(ApiResponse.success("创建成功", newUser));
 * 
 * // 错误响应
 * return ResponseEntity.badRequest()
 *     .body(ApiResponse.error("参数错误"));
 * 
 * // 错误响应（指定状态码）
 * return ResponseEntity.status(404)
 *     .body(ApiResponse.error(404, "资源不存在"));
 * ```
 * 
 * 【前端解析示例】
 * ```javascript
 * const response = await fetch('/api/users/1');
 * const result = await response.json();
 * 
 * if (result.success) {
 *   console.log('数据:', result.data);
 * } else {
 *   console.error('错误:', result.message);
 * }
 * ```
 * 
 * 【关联文件】
 * - 所有Controller: 使用此类封装响应
 * - GlobalExceptionHandler: 异常处理中使用
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class ApiResponse<T> {

    /**
     * 操作是否成功
     * 
     * - true: 请求成功，data字段包含有效数据
     * - false: 请求失败，message字段包含错误信息
     * 
     * 前端通常根据此字段判断请求是否成功
     */
    private boolean success;
    
    /**
     * 响应消息
     * 
     * 成功时：通常为"操作成功"或具体的成功描述
     * 失败时：包含具体的错误原因，便于前端展示
     * 
     * 示例：
     * - "操作成功"
     * - "用户名已存在"
     * - "参数验证失败：邮箱格式不正确"
     */
    private String message;
    
    /**
     * 响应数据
     * 
     * 使用泛型T，可以是任意类型的数据：
     * - 单个对象（如User）
     * - 列表（如List<User>）
     * - Map（如Map<String, Object>）
     * - 基本类型（如String, Integer）
     * - null（操作无返回数据时）
     */
    private T data;
    
    /**
     * 响应时间戳
     * 
     * 记录响应生成的时间，用于：
     * - 日志追踪
     * - 缓存控制
     * - 请求时序分析
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP状态码
     * 
     * 与HTTP响应状态码对应：
     * - 200: 成功
     * - 400: 客户端错误
     * - 500: 服务端错误
     * 
     * 前端可以根据code进行不同的错误处理
     */
    private int code;
    
    /**
     * 默认构造函数
     * 
     * 自动设置时间戳为当前时间
     * 通常由静态工厂方法调用，不直接使用
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 全参数构造函数
     * 
     * @param success 操作是否成功
     * @param message 响应消息
     * @param data 响应数据
     * @param code 状态码
     */
    public ApiResponse(boolean success, String message, T data, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
    
    // ==================== 静态工厂方法 ====================
    // 提供便捷的静态方法创建响应对象
    
    /**
     * 创建成功响应（默认消息）
     * 
     * 使用示例：
     * ```java
     * return ApiResponse.success(user);
     * ```
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的ApiResponse对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data, 200);
    }
    
    /**
     * 创建成功响应（自定义消息）
     * 
     * 使用示例：
     * ```java
     * return ApiResponse.success("创建成功", newUser);
     * ```
     * 
     * @param message 成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的ApiResponse对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }
    
    /**
     * 创建错误响应（默认状态码500）
     * 
     * 使用示例：
     * ```java
     * return ApiResponse.error("操作失败");
     * ```
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的ApiResponse对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, 500);
    }
    
    /**
     * 创建错误响应（自定义状态码）
     * 
     * 使用示例：
     * ```java
     * return ApiResponse.error(404, "资源不存在");
     * return ApiResponse.error(400, "参数验证失败");
     * ```
     * 
     * @param code 错误状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的ApiResponse对象
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, message, null, code);
    }
    
    // ==================== Getter和Setter方法 ====================
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
}
