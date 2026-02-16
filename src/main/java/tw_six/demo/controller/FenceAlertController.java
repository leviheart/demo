package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.service.FenceAlertService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 围栏告警控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供围栏告警记录的管理接口，是电子围栏监控系统的核心API。
 * 支持告警查询、处理、统计等功能，用于车辆越界行为的监控和记录。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法   │ 路径                              │ 功能描述                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET    │ /api/fence-alerts                 │ 获取所有告警记录           │
 * │ GET    │ /api/fence-alerts/unhandled       │ 获取未处理告警             │
 * │ GET    │ /api/fence-alerts/count/unhandled │ 获取未处理告警数量         │
 * │ GET    │ /api/fence-alerts/car/{carName}   │ 按车辆查询告警             │
 * │ GET    │ /api/fence-alerts/range           │ 按时间范围查询告警         │
 * │ PUT    │ /api/fence-alerts/{id}/handle     │ 处理告警                   │
 * │ POST   │ /api/fence-alerts/{id}/resolve    │ 解决告警                   │
 * │ DELETE │ /api/fence-alerts/{id}            │ 删除告警记录               │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 实时监控：车辆驶出/驶入围栏时自动触发告警
 * 2. 告警处理：调度人员确认并处理告警事件
 * 3. 历史查询：按车辆或时间范围查询历史告警
 * 4. 统计分析：统计未处理告警数量，用于仪表盘展示
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.FenceAlert
 * - 服务层: tw_six.demo.service.FenceAlertService
 * - 仓库层: tw_six.demo.repository.FenceAlertRepository
 * - 前端调用: FenceAlerts.vue组件
 * 
 * 【告警类型说明】
 * - ENTER: 进入围栏告警
 * - EXIT: 离开围栏告警
 * 
 * 【响应格式】
 * 所有接口统一使用ApiResponse包装返回结果:
 * {"code":200,"message":"success","data":[...]}
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/fence-alerts")
public class FenceAlertController {
    
    private final FenceAlertService fenceAlertService;
    
    @Autowired
    public FenceAlertController(FenceAlertService fenceAlertService) {
        this.fenceAlertService = fenceAlertService;
    }
    
    /**
     * 获取所有告警记录
     * 
     * 功能说明:
     * - 查询系统中所有的围栏告警记录
     * - 包含已处理和未处理的告警
     * - 用于告警管理页面的完整列表展示
     * 
     * @return 告警记录列表
     */
    @GetMapping
    public ApiResponse<List<FenceAlert>> getAllAlerts() {
        List<FenceAlert> alerts = fenceAlertService.getAllAlerts();
        return ApiResponse.success(alerts);
    }
    
    /**
     * 获取未处理告警列表
     * 
     * 功能说明:
     * - 查询所有状态为"未处理"的告警
     * - 用于告警提醒和待办事项展示
     * - 通常在首页或通知栏显示
     * 
     * @return 未处理告警列表
     */
    @GetMapping("/unhandled")
    public ApiResponse<List<FenceAlert>> getUnhandledAlerts() {
        List<FenceAlert> alerts = fenceAlertService.getUnhandledAlerts();
        return ApiResponse.success(alerts);
    }
    
    /**
     * 获取未处理告警数量
     * 
     * 功能说明:
     * - 统计当前未处理的告警总数
     * - 用于仪表盘数字展示和红点提醒
     * 
     * @return 未处理告警数量
     */
    @GetMapping("/count/unhandled")
    public ApiResponse<Long> getUnhandledAlertCount() {
        Long count = fenceAlertService.getUnhandledAlertCount();
        return ApiResponse.success(count);
    }
    
    /**
     * 按车辆名称查询告警记录
     * 
     * 功能说明:
     * - 查询指定车辆的所有围栏告警历史
     * - 用于车辆详情页面的告警记录展示
     * 
     * @param carName 车辆名称/车牌号
     * @return 该车辆的告警记录列表
     */
    @GetMapping("/car/{carName}")
    public ApiResponse<List<FenceAlert>> getAlertsByCar(@PathVariable String carName) {
        List<FenceAlert> alerts = fenceAlertService.getAlertsByCar(carName);
        return ApiResponse.success(alerts);
    }
    
    /**
     * 按时间范围查询告警记录
     * 
     * 功能说明:
     * - 查询指定时间范围内的告警记录
     * - 用于报表统计和历史查询功能
     * - 时间格式: ISO 8601 (如: 2024-01-15T00:00:00)
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的告警记录列表
     */
    @GetMapping("/range")
    public ApiResponse<List<FenceAlert>> getAlertsByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<FenceAlert> alerts = fenceAlertService.getAlertsByTimeRange(startTime, endTime);
        return ApiResponse.success(alerts);
    }
    
    /**
     * 处理告警（PUT方式）
     * 
     * 功能说明:
     * - 将告警状态更新为"已处理"
     * - 记录处理人和处理时间
     * - 用于告警确认和处理流程
     * 
     * @param id      告警ID
     * @param handler 处理人姓名/账号
     * @return 更新后的告警对象
     */
    @PutMapping("/{id}/handle")
    public ApiResponse<FenceAlert> handleAlert(@PathVariable Long id, @RequestParam String handler) {
        try {
            FenceAlert handledAlert = fenceAlertService.handleAlert(id, handler);
            return ApiResponse.success("告警处理成功", handledAlert);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 解决告警（POST方式）
     * 
     * 功能说明:
     * - 与handleAlert功能相同，但使用POST方法
     * - 请求体包含处理人信息
     * - 适配前端不同的调用方式
     * 
     * @param id   告警ID
     * @param body 请求体，包含handler字段
     * @return 更新后的告警对象
     */
    @PostMapping("/{id}/resolve")
    public ApiResponse<FenceAlert> resolveAlert(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            String handler = body.getOrDefault("handler", "system");
            FenceAlert handledAlert = fenceAlertService.handleAlert(id, handler);
            return ApiResponse.success("告警处理成功", handledAlert);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 删除告警记录
     * 
     * 功能说明:
     * - 物理删除指定的告警记录
     * - 通常用于清理测试数据或误报记录
     * - 正式环境建议使用软删除
     * 
     * @param id 要删除的告警ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlert(@PathVariable Long id) {
        try {
            fenceAlertService.deleteAlert(id);
            return ApiResponse.success("告警记录删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}
