package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.DrivingStatistics;
import tw_six.demo.service.DrivingStatisticsService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 驾驶统计控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆驾驶统计数据的管理接口，支持里程统计、排名查询、汇总分析等功能。
 * 用于生成运营报表和驾驶行为分析。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                                │ 功能描述                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST │ /api/statistics                     │ 保存统计数据               │
 * │ GET  │ /api/statistics/car/{carName}       │ 按车辆查询统计             │
 * │ GET  │ /api/statistics/car/{carName}/range │ 按日期范围查询统计         │
 * │ GET  │ /api/statistics/daily-ranking       │ 获取日里程排名             │
 * │ GET  │ /api/statistics/summary/{carName}   │ 获取车辆统计汇总           │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 日报生成：每日自动汇总车辆行驶里程
 * 2. 绩效考核：统计驾驶员工作量和效率
 * 3. 运营分析：分析车队整体运营情况
 * 4. 成本核算：根据里程计算燃油和维护成本
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.DrivingStatistics
 * - 服务层: tw_six.demo.service.DrivingStatisticsService
 * - 仓库层: tw_six.demo.repository.DrivingStatisticsRepository
 * 
 * 【数据模型】
 * DrivingStatistics包含:
 * - carName: 车辆名称
 * - date: 统计日期
 * - totalMileage: 总里程（公里）
 * - totalDuration: 总时长（分钟）
 * - averageSpeed: 平均速度
 * - maxSpeed: 最高速度
 * 
 * 【统计周期】
 * - 按日统计：每日凌晨自动汇总前一天数据
 * - 按周/月统计：基于日统计数据聚合
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/statistics")
public class DrivingStatisticsController {
    
    private final DrivingStatisticsService drivingStatisticsService;
    
    @Autowired
    public DrivingStatisticsController(DrivingStatisticsService drivingStatisticsService) {
        this.drivingStatisticsService = drivingStatisticsService;
    }
    
    /**
     * 保存驾驶统计数据
     * 
     * 功能说明:
     * - 保存一条驾驶统计记录
     * - 通常由定时任务自动调用，也可手动录入
     * 
     * @param statistics 统计数据对象
     * @return 保存后的统计对象
     */
    @PostMapping
    public ApiResponse<DrivingStatistics> saveStatistics(@RequestBody DrivingStatistics statistics) {
        DrivingStatistics savedStats = drivingStatisticsService.saveStatistics(statistics);
        return ApiResponse.success("统计数据保存成功", savedStats);
    }
    
    /**
     * 按车辆查询统计数据
     * 
     * 功能说明:
     * - 查询指定车辆的所有历史统计记录
     * - 按日期倒序排列
     * 
     * @param carName 车辆名称
     * @return 该车辆的统计记录列表
     */
    @GetMapping("/car/{carName}")
    public ApiResponse<List<DrivingStatistics>> getStatisticsByCar(@PathVariable String carName) {
        List<DrivingStatistics> statistics = drivingStatisticsService.getStatisticsByCar(carName);
        return ApiResponse.success(statistics);
    }
    
    /**
     * 按日期范围查询统计数据
     * 
     * 功能说明:
     * - 查询指定车辆在特定日期范围内的统计
     * - 用于生成周报、月报等周期性报表
     * 
     * @param carName   车辆名称
     * @param startDate 开始日期（格式: yyyy-MM-dd）
     * @param endDate   结束日期（格式: yyyy-MM-dd）
     * @return 日期范围内的统计记录列表
     */
    @GetMapping("/car/{carName}/range")
    public ApiResponse<List<DrivingStatistics>> getStatisticsByDateRange(
        @PathVariable String carName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DrivingStatistics> statistics = drivingStatisticsService.getStatisticsByDateRange(carName, startDate, endDate);
        return ApiResponse.success(statistics);
    }
    
    /**
     * 获取日里程排名
     * 
     * 功能说明:
     * - 查询指定日期所有车辆的里程排名
     * - 按里程从高到低排序
     * - 用于绩效展示和竞赛排名
     * 
     * @param date 查询日期（格式: yyyy-MM-dd）
     * @return 当日里程排名列表
     */
    @GetMapping("/daily-ranking")
    public ApiResponse<List<DrivingStatistics>> getDailyRanking(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DrivingStatistics> ranking = drivingStatisticsService.getDailyRanking(date);
        return ApiResponse.success(ranking);
    }
    
    /**
     * 获取车辆统计汇总
     * 
     * 功能说明:
     * - 汇总指定车辆的整体统计数据
     * - 包含平均里程、总里程、最新统计等
     * - 用于车辆详情页的概览展示
     * 
     * @param carName 车辆名称
     * @return 汇总数据Map，包含多个统计指标
     */
    @GetMapping("/summary/{carName}")
    public ApiResponse<Map<String, Object>> getCarSummary(@PathVariable String carName) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageMileage", drivingStatisticsService.getAverageMileage(carName));
        summary.put("totalMileage", drivingStatisticsService.getTotalMileage(carName));
        summary.put("latestStats", drivingStatisticsService.getLatestStatistics(carName));
        return ApiResponse.success(summary);
    }
}
