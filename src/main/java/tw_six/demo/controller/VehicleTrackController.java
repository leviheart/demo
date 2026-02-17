package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.dto.TrackPlaybackResponse;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.service.VehicleTrackService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 车辆轨迹控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆轨迹数据的管理接口，支持轨迹记录的保存、查询、时间范围检索等功能。
 * 轨迹数据用于轨迹回放、历史路线分析等场景。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                                 │ 功能描述                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST │ /api/tracks                          │ 添加轨迹记录              │
 * │ GET  │ /api/tracks/car/{carName}            │ 按车辆查询轨迹            │
 * │ GET  │ /api/tracks/car/{carName}/recent     │ 查询最近轨迹              │
 * │ GET  │ /api/tracks/cars                     │ 获取所有车辆名称          │
 * │ GET  │ /api/tracks/car/{carName}/range      │ 按时间范围查询轨迹        │
 * │ GET  │ /api/tracks/car/{carName}/playback   │ 获取轨迹回放数据          │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 轨迹回放：在地图上回放车辆历史行驶路线
 * 2. 路线分析：分析车辆常走路线和行驶习惯
 * 3. 位置追踪：实时追踪车辆当前位置和历史轨迹
 * 4. 里程计算：根据轨迹点计算实际行驶里程
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.VehicleTrack
 * - 服务层: tw_six.demo.service.VehicleTrackService
 * - 仓库层: tw_six.demo.repository.VehicleTrackRepository
 * - 前端调用: MapView.vue组件（轨迹回放功能）
 * 
 * 【数据特点】
 * - 轨迹数据量大，需要定期归档
 * - 建议按时间分区存储
 * - 查询时注意分页和索引优化
 * 
 * 【轨迹点结构】
 * 每个轨迹点包含:
 * - carName: 车辆标识
 * - latitude, longitude: GPS坐标
 * - timestamp: 时间戳
 * - speed: 速度
 * - direction: 方向
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/tracks")
public class VehicleTrackController {
    
    private final VehicleTrackService vehicleTrackService;
    
    @Autowired
    public VehicleTrackController(VehicleTrackService vehicleTrackService) {
        this.vehicleTrackService = vehicleTrackService;
    }
    
    /**
     * 添加轨迹记录
     * 
     * 功能说明:
     * - 保存一条新的轨迹点记录
     * - 通常由GPS设备定时上报调用
     * - 轨迹点按时间顺序存储
     * 
     * @param track 轨迹点对象
     * @return 保存后的轨迹对象
     */
    @PostMapping
    public ApiResponse<VehicleTrack> addTrack(@RequestBody VehicleTrack track) {
        VehicleTrack savedTrack = vehicleTrackService.saveTrack(track);
        return ApiResponse.success("轨迹记录保存成功", savedTrack);
    }
    
    /**
     * 按车辆查询所有轨迹
     * 
     * 功能说明:
     * - 查询指定车辆的所有历史轨迹点
     * - 数据量可能很大，建议配合时间范围使用
     * 
     * @param carName 车辆名称
     * @return 该车辆的轨迹点列表
     */
    @GetMapping("/car/{carName}")
    public ApiResponse<List<VehicleTrack>> getTracksByCar(@PathVariable String carName) {
        List<VehicleTrack> tracks = vehicleTrackService.getTracksByCarName(carName);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 查询最近轨迹
     * 
     * 功能说明:
     * - 查询指定车辆最近N分钟内的轨迹
     * - 用于实时追踪和轨迹回放
     * - 默认查询最近60分钟
     * 
     * @param carName 车辆名称
     * @param minutes 时间范围（分钟），默认60
     * @return 最近时间段内的轨迹点列表
     */
    @GetMapping("/car/{carName}/recent")
    public ApiResponse<List<VehicleTrack>> getRecentTracks(
        @PathVariable String carName,
        @RequestParam(defaultValue = "60") int minutes) {
        List<VehicleTrack> tracks = vehicleTrackService.getRecentTracks(carName, minutes);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 获取所有车辆名称列表
     * 
     * 功能说明:
     * - 查询系统中有轨迹记录的所有车辆
     * - 用于车辆选择下拉框
     * 
     * @return 车辆名称列表
     */
    @GetMapping("/cars")
    public ApiResponse<List<String>> getAllCarNames() {
        List<String> carNames = vehicleTrackService.getAllCarNames();
        return ApiResponse.success(carNames);
    }
    
    /**
     * 按时间范围查询轨迹
     * 
     * 功能说明:
     * - 查询指定车辆在特定时间范围内的轨迹
     * - 用于轨迹回放和历史查询
     * - 时间格式: ISO 8601 (如: 2024-01-15T08:00:00)
     * 
     * @param carName   车辆名称
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的轨迹点列表
     */
    @GetMapping("/car/{carName}/range")
    public ApiResponse<List<VehicleTrack>> getTracksByTimeRange(
        @PathVariable String carName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<VehicleTrack> tracks = vehicleTrackService.getTracksByTimeRange(carName, startTime, endTime);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 获取轨迹回放数据
     * 
     * 功能说明:
     * - 获取指定车辆在时间范围内的轨迹回放数据
     * - 包含轨迹点列表、统计信息（总距离、时长、平均速度等）
     * - 每个轨迹点包含序号、累计距离、回放进度等信息
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间（可选，默认最近24小时）
     * @param endTime 结束时间（可选，默认当前时间）
     * @param hours 最近N小时（可选，与时间范围二选一）
     * @return 轨迹回放响应数据
     */
    @GetMapping("/car/{carName}/playback")
    public ApiResponse<TrackPlaybackResponse> getPlaybackData(
        @PathVariable String carName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(required = false, defaultValue = "24") Integer hours) {
        
        TrackPlaybackResponse response;
        
        if (startTime != null && endTime != null) {
            // 使用指定时间范围
            response = vehicleTrackService.getPlaybackData(carName, startTime, endTime);
        } else {
            // 使用最近N小时
            response = vehicleTrackService.getRecentPlaybackData(carName, hours);
        }
        
        return ApiResponse.success(response);
    }
}
