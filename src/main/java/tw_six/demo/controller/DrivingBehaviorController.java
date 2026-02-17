package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.DrivingBehavior;
import tw_six.demo.service.DrivingBehaviorService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 驾驶行为分析控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供驾驶行为分析相关的API接口，支持行为记录查询、行为分析、驾驶评分等功能。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                                    │ 功能描述               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET  │ /api/behaviors/car/{carName}            │ 按车辆查询行为记录     │
 * │ GET  │ /api/behaviors/car/{carName}/range      │ 按时间范围查询行为     │
 * │ GET  │ /api/behaviors/car/{carName}/analyze    │ 分析驾驶行为           │
 * │ GET  │ /api/behaviors/car/{carName}/score      │ 获取驾驶评分           │
 * │ GET  │ /api/behaviors/recent                   │ 获取最近行为记录       │
 * │ GET  │ /api/behaviors/cars                     │ 获取所有车辆名称       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【关联文件】
 * - DrivingBehaviorService.java: 服务层
 * - DrivingBehavior.java: 实体类
 * - DrivingBehaviorRepository.java: 数据仓库
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/behaviors")
public class DrivingBehaviorController {

    private final DrivingBehaviorService behaviorService;

    @Autowired
    public DrivingBehaviorController(DrivingBehaviorService behaviorService) {
        this.behaviorService = behaviorService;
    }

    /**
     * 按车辆查询所有行为记录
     * 
     * @param carName 车辆名称
     * @return 行为记录列表
     */
    @GetMapping("/car/{carName}")
    public ApiResponse<List<DrivingBehavior>> getBehaviorsByCar(@PathVariable String carName) {
        List<DrivingBehavior> behaviors = behaviorService.getBehaviorsByCar(carName);
        return ApiResponse.success(behaviors);
    }

    /**
     * 按时间范围查询行为记录
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为记录列表
     */
    @GetMapping("/car/{carName}/range")
    public ApiResponse<List<DrivingBehavior>> getBehaviorsByTimeRange(
            @PathVariable String carName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<DrivingBehavior> behaviors = behaviorService.getBehaviorsByTimeRange(carName, startTime, endTime);
        return ApiResponse.success(behaviors);
    }

    /**
     * 分析驾驶行为
     * 
     * 分析指定车辆在时间范围内的轨迹数据，检测异常驾驶行为。
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 检测到的行为列表
     */
    @GetMapping("/car/{carName}/analyze")
    public ApiResponse<List<DrivingBehavior>> analyzeBehavior(
            @PathVariable String carName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<DrivingBehavior> behaviors = behaviorService.analyzeDrivingBehavior(carName, startTime, endTime);
        return ApiResponse.success("分析完成，检测到 " + behaviors.size() + " 个异常行为", behaviors);
    }

    /**
     * 获取驾驶评分
     * 
     * 计算指定车辆在时间范围内的驾驶评分。
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 驾驶评分结果
     */
    @GetMapping("/car/{carName}/score")
    public ApiResponse<DrivingBehaviorService.DrivingScore> getDrivingScore(
            @PathVariable String carName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        DrivingBehaviorService.DrivingScore score = behaviorService.calculateDrivingScore(carName, startTime, endTime);
        return ApiResponse.success(score);
    }

    /**
     * 获取最近的行为记录
     * 
     * @param limit 限制数量，默认20
     * @return 最近的行为记录列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<DrivingBehavior>> getRecentBehaviors(
            @RequestParam(defaultValue = "20") int limit) {
        List<DrivingBehavior> behaviors = behaviorService.getRecentBehaviors(limit);
        return ApiResponse.success(behaviors);
    }

    /**
     * 获取所有有行为记录的车辆名称
     * 
     * @return 车辆名称列表
     */
    @GetMapping("/cars")
    public ApiResponse<List<String>> getAllCarNames() {
        List<String> carNames = behaviorService.getAllCarNames();
        return ApiResponse.success(carNames);
    }
}
