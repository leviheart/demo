package tw_six.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tw_six.demo.common.ApiResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器 - 服务监控API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供服务健康状态检查接口，用于监控系统的运行状态。
 * 支持负载均衡器、容器编排系统（如Kubernetes）的健康探测。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径     │ 功能描述                                           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET  │ /health  │ 服务健康状态检查                                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 负载均衡：负载均衡器定期探测，判断服务是否可用
 * 2. 容器健康：Kubernetes liveness/readiness探针
 * 3. 监控告警：监控系统定时检查服务状态
 * 4. 运维管理：快速判断服务是否正常运行
 * 
 * 【关联文件】
 * - 通用响应: tw_six.demo.common.ApiResponse
 * 
 * 【响应示例】
 * {
 *   "code": 200,
 *   "message": "服务运行正常",
 *   "data": {
 *     "status": "UP",
 *     "timestamp": 1705312800000,
 *     "service": "demo-service"
 *   }
 * }
 * 
 * 【扩展建议】
 * - 可添加数据库连接检查
 * - 可添加外部服务依赖检查
 * - 可添加JVM内存使用情况
 * - 可添加磁盘空间检查
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
public class HealthController {
    
    /**
     * 服务健康状态检查
     * 
     * 功能说明:
     * - 返回服务当前运行状态
     * - 包含服务标识和时间戳
     * - 如果服务能响应，说明基本功能正常
     * 
     * 状态说明:
     * - UP: 服务正常运行
     * - DOWN: 服务异常（需要扩展实现检测逻辑）
     * 
     * @return 健康状态信息Map
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("service", "demo-service");
        return ApiResponse.success("服务运行正常", healthInfo);
    }
}
